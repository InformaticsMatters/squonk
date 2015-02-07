package com.im.lac.demo.services

import com.im.lac.demo.model.DataItem
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
    static final String DEMO_FILES_TABLE_NAME = 'users.demo_files'
    
    DbFileService(DataSource dataSource) {
        this.dataSource = dataSource
    }
    
    void createTables() {
        
        Sql db = new Sql(dataSource)
        try {
            db.firstRow('select count(*) from ' + DEMO_FILES_TABLE_NAME)
        } catch (SQLException se) {
            createTable(db)
        }
        
    }
    
    private void createTable(Sql db) {
        log.info("Creating table")
        db.execute """\
            |CREATE TABLE $DEMO_FILES_TABLE_NAME (
            |  id SERIAL PRIMARY KEY,
            |  name TEXT NOT NULL,
            |  time_created TIMESTAMP NOT NULL DEFAULT NOW(),
            |  last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
            |  size INTEGER,
            |  loid BIGINT NOT NULL
            |)""".stripMargin()
    }
    
    DataItem addDataItem(DataItem data, InputStream is) {
        
        Connection con = dataSource.connection
        Sql db = new Sql(con)
        Long id
        db.withTransaction {
            Long loid = createLargeObject(is, con)
            def gen = db.executeInsert("""\
            |INSERT INTO $DEMO_FILES_TABLE_NAME (name, size, loid)
            |  VALUES (?,?,?)""".stripMargin(), [data.name, data.size, loid]) 
            id = gen[0][0]
            
            log.info("Created data item with id $id using loid $loid")
        }
        return loadDataItem(id)
    }
    
    DataItem updateDataItem(DataItem data) {
        
        Connection con = dataSource.connection
        Sql db = new Sql(con)
        Long id = data.id
        db.withTransaction {
            db.executeUpdate("""\
            |UPDATE $DEMO_FILES_TABLE_NAME set name = ?, size = ?, last_updated = NOW()
            |  WHERE id = ?""".stripMargin(), [data.name, data.size, id]) 

            log.info("Updated data item with id $id")
        }
        return loadDataItem(id)
    }
    
    DataItem updateDataItem(DataItem data, InputStream is) {
        
        Connection con = dataSource.connection
        Sql db = new Sql(con)
        Long id = data.id
        db.withTransaction {
            deleteLargeObject(data.loid, con)
            Long loid = createLargeObject(is, con)
            db.executeUpdate("""\
            |UPDATE $DEMO_FILES_TABLE_NAME set loid = ?, last_updated = NOW()
            |  WHERE id = ?""".stripMargin(), [loid, id]) 
            
            log.info("Created data item with id $id using loid $loid")
        }
        return loadDataItem(id)
    }
    
    DataItem loadDataItem(Long id) {
        log.fine("loadDataItem($id)")
        DataItem data = new DataItem()
        Sql db = new Sql(dataSource)
        db.withTransaction {
            def row = db.firstRow('SELECT * FROM ' + DbFileService.DEMO_FILES_TABLE_NAME + ' WHERE id = ?', [id])
            data = buildDataItem(row)
        }
        return data
    }
    
    List<DataItem> loadDataItems() {
        log.fine("loadDataItems()")
        List<DataItem> items = []
        Sql db = new Sql(dataSource)
        db.withTransaction {
            db.eachRow('SELECT * FROM ' + DbFileService.DEMO_FILES_TABLE_NAME) { row ->
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
        data.created = row.time_created
        data.updated = row.last_updated
        data.loid = row.loid
        return data
    }
    
    void deleteDataItem(DataItem data) {
        final Connection con = dataSource.connection
        con.setAutoCommit(false);
        try {
            deleteLargeObject(data.loid, con)
            Sql db = new Sql(con)
            db.executeUpdate("DELETE FROM " + DEMO_FILES_TABLE_NAME + " WHERE id = ?", [data.id])
        } finally {
            con.commit()
        }
    }
    
    private long createLargeObject(InputStream is, Connection con) {
        
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
    
    InputStream createLargeObjectReader(final long loid) {
        
        log.info("createLargeObjectReader(${loid})")
        final Connection con = dataSource.connection
        con.setAutoCommit(false)
        // Get the Large Object Manager to perform operations with
        LargeObjectManager lobj = ((org.postgresql.PGConnection)con).getLargeObjectAPI()
        LargeObject obj = lobj.open(loid, LargeObjectManager.READ)

        return obj.getInputStream();     
    }

}