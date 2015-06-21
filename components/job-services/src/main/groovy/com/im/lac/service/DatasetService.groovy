package com.im.lac.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.im.lac.model.DataItem
import com.im.lac.types.io.Metadata
import groovy.sql.Sql
import groovy.util.logging.Log
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.function.Consumer
import java.util.function.Function
import javax.sql.DataSource
import org.postgresql.largeobject.LargeObject
import org.postgresql.largeobject.LargeObjectManager
import org.apache.camel.util.IOHelper


/**
 *
 * @author timbo
 */
@Log
class DatasetService {
    
    DataSource dataSource
    static public final String DEFAULT_TABLE_NAME = 'users.demo_files'
    private final String tableName;
    private final ObjectMapper objectMapper;
    
    DatasetService(DataSource dataSource) {
        this(dataSource, DEFAULT_TABLE_NAME);
    }
    
    /** Alternative constructor allowing the table name to be specified, primarily 
     * for testing purposes.
     */
    DatasetService(DataSource dataSource, String tableName) {
        this.dataSource = dataSource
        this.tableName = tableName;
        this.objectMapper = new ObjectMapper();
    }
    
    /** Deletes all data and sets up some test data. FOR TESTING PURPOSES ONLY.
     * 
     */
    protected List<Long> createTestData() {

        def ids = []
        doInTransaction { db ->
            db.execute 'DELETE FROM ' + tableName
            ids << addDataItem(db, new DataItem(name: 'test0', size: 1, metadata:new Metadata(type:Metadata.Type.TEXT,size:1,className:"java.lang.String")), new ByteArrayInputStream('World'.bytes)).id
            ids << addDataItem(db, new DataItem(name: 'test1', size: 3, metadata:new Metadata(type:Metadata.Type.ARRAY,size:3,className:"java.lang.String")), new ByteArrayInputStream('''["one", "two", "three"]'''.bytes)).id
            ids << addDataItem(db, new DataItem(name: 'test2', size: 4, metadata:new Metadata(type:Metadata.Type.ARRAY,size:4,className:"java.lang.String")), new ByteArrayInputStream('''["red", "yellow", "green", "blue"]'''.bytes)).id
            ids << addDataItem(db, new DataItem(name: 'test3', size: 5, metadata:new Metadata(type:Metadata.Type.ARRAY,size:5,className:"java.lang.String")), new ByteArrayInputStream('''["banana", "pineapple", "orange", "apple", "pear"]'''.bytes)).id
        }
        return ids
    }
    
    void createTables() { 
        Sql db = new Sql(dataSource)
        try {
            db.firstRow('select count(*) from ' + tableName)
        } catch (SQLException se) {
            createTable(db)
        }
    }
    
    private void createTable(Sql db) {
        log.info("Creating table")
        db.execute """\
            |CREATE TABLE $tableName (
            |  id SERIAL PRIMARY KEY,
            |  name TEXT NOT NULL,
            |  time_created TIMESTAMP NOT NULL DEFAULT NOW(),
            |  last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
            |  size INTEGER,
            |  metadata JSONB,
            |  loid BIGINT NOT NULL
            |)""".stripMargin()
    }
    
    //    /**
    //     * Get a connection for externally managed transactions
    //     */
    //    private Connection getConnection() {
    //        return dataSource.getConnection();
    //    } 
    
    /** This is the entry point for getting a Sql instance
     */
    public <R> R doInTransactionWithResult(Class<R> type, Function<Sql,R> executable) throws Exception {
        Sql db = new Sql(dataSource.connection)
        R result
        db.withTransaction {
            result = executable.apply(db)
        }
        return result
    }
    
    public void doInTransaction(Consumer<Sql> executable) throws Exception {
        Sql db = new Sql(dataSource.connection)
        db.withTransaction {
            executable.accept(db)
        }
    }
    
    DataItem addDataItem(final DataItem data, final InputStream is) throws Exception {
        return doInTransactionWithResult(DataItem.class) { addDataItem(it, data, is) }
    }
    
    /**
     * Add a new data item with content.
     */
    DataItem addDataItem(final Sql db, final DataItem data, final InputStream is) throws Exception {
        
        Long loid = createLargeObject(db, is)
        String metaJson = marshalMetadata(data.metadata);
        def gen = db.executeInsert("""\
            |INSERT INTO $tableName (name, size, metadata, loid)
            |  VALUES (?,?,?::jsonb,?)""".stripMargin(), [data.name, data.size, metaJson, loid]) 
        Long id = gen[0][0]
            
        log.info("Created data item with id $id using loid $loid")
        return getDataItem(db, id)
    }
    
    DataItem updateDataItem(final DataItem data) throws Exception {
        return doInTransactionWithResult(DataItem.class) { updateDataItem(it, data) }
    }
    
    DataItem updateDataItem(final Sql db, final DataItem data) throws Exception {     
        Long id = data.id
        String metaJson = marshalMetadata(data.metadata)
        db.executeUpdate("""\
            |UPDATE $tableName set name = ?, size = ?, metadata = ?::jsonb, last_updated = NOW()
            |  WHERE id = ?""".stripMargin(), [data.name, data.size, metaJson, id]) 

        log.info("Updated data item with id $id")
        return getDataItem(db, id)
    }
    
    DataItem updateDataItem(final DataItem data, final InputStream is) throws Exception {
        return doInTransactionWithResult(DataItem.class) { updateDataItem(it, data, is) }
    }
    
    DataItem updateDataItem(final Sql db, final DataItem data, final InputStream is) throws Exception {
        Long id = data.id
        deleteLargeObject(db, data.loid)
        Long loid = createLargeObject(db, is)
        String metaJson = marshalMetadata(data.metadata);
        db.executeUpdate("""\
            |UPDATE $tableName set loid = ?, last_updated = NOW(), metadata = ?::jsonb
            |  WHERE id = ?""".stripMargin(), [loid, metaJson, id]) 
            
        log.info("Created data item with id $id using loid $loid")
        return getDataItem(db, data.id)
    }
    
    DataItem getDataItem(final Long id) throws Exception { 
        return doInTransactionWithResult(DataItem.class) { getDataItem(it, id) }
    }
    
    DataItem getDataItem(final Sql db, final Long id) {
        log.fine("getDataItem($id)")
        def row = db.firstRow('SELECT id, name, time_created, last_updated, size, metadata::text, loid FROM ' 
            + tableName + ' WHERE id = ?', [id])
        if (!row) {
            throw new IllegalArgumentException("Item with ID $id not found")
        }
        DataItem data = buildDataItem(row)
        return data
    }
    
    List<DataItem> getDataItems() throws Exception {
        return doInTransactionWithResult(List.class) { getDataItems(it) }
    }
    
    List<DataItem> getDataItems(final Sql db) throws Exception {
        log.fine("getDataItems()")
        List<DataItem> items = []
        long t0 = System.currentTimeMillis()
        long t1 = System.currentTimeMillis()
        println "Creating Sql took " + (t1-t0)
        db.eachRow('SELECT id, name, time_created, last_updated, size, metadata::text, loid FROM ' 
            + tableName + '  ORDER BY id') { row ->
            items << buildDataItem(row)
        }
        return items
    }
    
    private DataItem buildDataItem(def row) {
        DataItem data = new DataItem()
        data.id = row.id
        data.name = row.name
        data.size = row.size
        data.metadata = unmarshalMetadata(row.metadata)
        data.created = row.time_created
        data.updated = row.last_updated
        data.loid = row.loid
        return data
    }
    
    void deleteDataItem(final DataItem data) throws Exception {
        doInTransaction() { deleteDataItem(it, data) }
    }
    
    /** Delete data item within a new transaction
     */
    void deleteDataItem(final Sql db, final DataItem data) throws Exception {
        deleteLargeObject(db, data.loid)
        db.executeUpdate("DELETE FROM " + tableName + " WHERE id = ?", [data.id])    
    }
    
    private long createLargeObject(final Sql db, final InputStream is) {
        
        // Get the Large Object Manager to perform operations with
        LargeObjectManager lobj = ((org.postgresql.PGConnection)db.connection).getLargeObjectAPI();
        // Create a new large object
        long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE)
        // Open the large object for writing
        LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE)
        try {
            // Copy the data from the stream to the large object
            byte[] buf = new byte[2048]
            int s, tl = 0;
            while ((s = is.read(buf, 0, 2048)) > 0) {
                obj.write(buf, 0, s)
                tl += s
            }
        } finally {
            obj.close()
        }
        log.info("Created large object with id $oid")
        return oid
    }
    
    void deleteLargeObject(final Long loid) {
        doInTransaction() { deleteLargeObject(it, loid) }
    }
    
    void deleteLargeObject(final Sql db, final Long loid) {
        
        // Get the Large Object Manager to perform operations with
        final LargeObjectManager lobj = ((org.postgresql.PGConnection)db.connection).getLargeObjectAPI();
        lobj.delete(loid)
    }
    
//    InputStream createLargeObjectReader(final long loid) {
//        return doInTransactionWithResult(List.class) { createLargeObjectReader(it, loid) }
//    }
    
    /**
     * Create an InputStream that reads the large object. The connection must be 
     * in a transaction (autoCommit = false) and the InputStream MUST be closed when 
     * finished with
     */
    InputStream createLargeObjectReader(final Sql db, final long loid) throws Exception {
        // Get the Large Object Manager to perform operations with
        LargeObjectManager lobj = ((org.postgresql.PGConnection)db.connection).getLargeObjectAPI()
        LargeObject obj = lobj.open(loid, LargeObjectManager.READ)
        return obj.getInputStream();        
    }
    
    private String marshalMetadata(Metadata meta) {
        return objectMapper.writeValueAsString(meta);
    }
    
    private Metadata unmarshalMetadata(String json) {
        if (json != null) {
            return objectMapper.readValue(json, Metadata.class);
        } else {
            return new Metadata();
        }
    }
    
}