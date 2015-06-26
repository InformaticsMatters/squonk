package com.im.lac.demo.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.im.lac.demo.model.DataItem
import com.im.lac.dataset.Metadata
import groovy.sql.Sql
import groovy.util.logging.Log
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import javax.sql.DataSource
import org.postgresql.largeobject.LargeObject
import org.postgresql.largeobject.LargeObjectManager
import org.apache.camel.util.IOHelper


/**
 *
 * @author timbo
 */
@Log
class DbFileService {
    
    DataSource dataSource
    static final String DEFAULT_TABLE_NAME = 'users.demo_files'
    private final String tableName;
    private final ObjectMapper objectMapper;
    
    DbFileService(DataSource dataSource) {
        this(dataSource, DEFAULT_TABLE_NAME);
    }
    
    /** Alternative constructor allowing the table name to be specified, primarily 
     * for testing purposes.
     */
    DbFileService(DataSource dataSource, String tableName) {
        this.dataSource = dataSource
        this.tableName = tableName;
        this.objectMapper = new ObjectMapper();
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
    
    /**
     * Get a connection for externally managed transactions
     */
    public Connection getConnection() {
        return dataSource.getConnection();
    } 
    
    /** 
     * Add a new data item within a new transaction
     */
    DataItem addDataItem(DataItem data, InputStream is) {
        Sql db = new Sql(dataSource.connection)
        DataItem item
        db.withTransaction {
            item = doAddDataItem(db, data, is)
        }
        return item
    }
    
    /**
     * Add a new data item within a transaction that is managed externally.
     */
    DataItem addDataItem(Connection con, DataItem data, InputStream is) {
        Sql db = new Sql(con)
        return  doAddDataItem(db, data, is)
    }
    
    private DataItem doAddDataItem(Sql db, DataItem data, InputStream is) {
        String metaJson = marshalMetadata(data.metadata);
        Long loid = createLargeObject(db.connection, is)
        def gen = db.executeInsert("""\
            |INSERT INTO $tableName (name, size, metadata, loid)
            |  VALUES (?,?,?::jsonb,?)""".stripMargin(), [data.name, data.size, metaJson, loid]) 
        Long id = gen[0][0]
            
        log.info("Created data item with id $id using loid $loid")
        return doLoadDataItem(db, id)
    }
    
    DataItem updateDataItem(DataItem data) {
        Connection con = dataSource.connection
        Sql db = new Sql(con)
        DataItem neu
        db.withTransaction {
            neu = doUpdateDataItem(db, data)
        }
        return neu
    }
    
    DataItem updateDataItem(Connection con, DataItem data) {
        Sql db = new Sql(con)
        return doUpdateDataItem(db, data)
    }
    
    private DataItem doUpdateDataItem(Sql db, DataItem data) {
        
        Long id = data.id
        String metaJson = marshalMetadata(data.metadata);
        db.executeUpdate("""\
            |UPDATE $tableName set name = ?, size = ?, metadata = ?::jsonb, last_updated = NOW()
            |  WHERE id = ?""".stripMargin(), [data.name, data.size, metaJson, id]) 

        log.info("Updated data item with id $id")
        return doLoadDataItem(db, id)
    }
    
    
    DataItem updateDataItem(DataItem data, InputStream is) {
        Connection con = dataSource.connection
        Sql db = new Sql(con)
        DataItem neu
        db.withTransaction {
            neu = doUpdateDataItem(db, data, is)
        }
        return neu
    }
    
    DataItem updateDataItem(Connection con, DataItem data, InputStream is) {
        Sql db = new Sql(con)
        return doUpdateDataItem(db, data, is)
    }
    
    private DataItem doUpdateDataItem(Sql db, DataItem data, InputStream is) {

        Long id = data.id
        Connection con = db.connection
        deleteLargeObject(data.loid, con)
        Long loid = createLargeObject(con, is)
        db.executeUpdate("""\
            |UPDATE $tableName set loid = ?, last_updated = NOW()
            |  WHERE id = ?""".stripMargin(), [loid, id]) 
            
        log.info("Created data item with id $id using loid $loid")
        return doLoadDataItem(db, data.id)
    }
    
    DataItem loadDataItem(Long id) {
        Sql db = new Sql(dataSource.connection)
        DataItem data = null
        db.withTransaction {
            data = doLoadDataItem(db, id)
        }
        return data
    }
    
    DataItem loadDataItem(Connection con, Long id) {        
        return doLoadDataItem(new Sql(connection), id)
    }
    
    private DataItem doLoadDataItem(Sql db, Long id) {
        log.fine("loadDataItem($id)")
        def row = db.firstRow('SELECT id, name, time_created, last_updated, size, metadata::text, loid FROM ' 
            + tableName + ' WHERE id = ?', [id])
        if (!row) {
            throw new IllegalArgumentException("Item with ID $id not found")
        }
        DataItem data = buildDataItem(row)
        return data
    }
    
    List<DataItem> loadDataItems() {
        log.fine("loadDataItems()")
        List<DataItem> items = []
        Sql db = new Sql(dataSource)
        db.withTransaction {
            db.eachRow('SELECT id, name, time_created, last_updated, size, metadata::text, loid FROM ' 
                + tableName + '  ORDER BY id') { row ->
                items << buildDataItem(row)
            }
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
    
    /** Delete data item within an externally managed transaction
     */
    void deleteDataItem(Connection con, DataItem data) {
        doDeleteDataItem(new Sql(con), data)    
    }
    
    /** Delete data item within a new transaction
     */
    void deleteDataItem(DataItem data) {
        final Connection con = dataSource.connection
        Sql db = new Sql(con)
        db.withTransaction {
            doDeleteDataItem(db, data)
        } 
    }
    
    private void doDeleteDataItem(Sql db, DataItem data) {
        deleteLargeObject(data.loid, db.connection)
        db.executeUpdate("DELETE FROM " + tableName + " WHERE id = ?", [data.id])    
    }
    
    private long createLargeObject(Connection con, InputStream is) {
        
        // Get the Large Object Manager to perform operations with
        LargeObjectManager lobj = ((org.postgresql.PGConnection)con).getLargeObjectAPI();
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
    
    void deleteLargeObject(final Long loid, Connection con) {
        
        // Get the Large Object Manager to perform operations with
        final LargeObjectManager lobj = ((org.postgresql.PGConnection)con).getLargeObjectAPI();
        lobj.delete(loid)
    }
    
    /**
     * Create an InputStream that reads the large object. The connection must be 
     * in a transaction (autoCommit = false) and the InputStream MUST be closed when 
     * finished with
     */
    InputStream createLargeObjectReader(Connection con, long loid) {
        // Get the Large Object Manager to perform operations with
        LargeObjectManager lobj = ((org.postgresql.PGConnection)con).getLargeObjectAPI()
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