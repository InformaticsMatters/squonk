package com.im.lac.demo.services

import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource
import spock.lang.Shared
import spock.lang.Specification
import com.im.lac.demo.model.*

/**
 *
 * @author timbo
 */
class DbFileServiceSpec extends Specification {
    
    @Shared DataSource dataSource = createDataSource()
    @Shared DbFileService service = new DbFileService(dataSource)
    @Shared Sql db = new Sql(dataSource)
    @Shared DataItem[] item = new DataItem[1] 
    
    DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource()
        
        ds.serverName = System.getenv("CHEMCENTRAL_DB_SERVER") ?: 'localhost'
        ds.portNumber =  new Integer(System.getenv("CHEMCENTRAL_DB_PORT") ?: '5432')
        ds.databaseName = System.getenv("CHEMCENTRAL_DB_NAME") ?: 'chemcentral'
        ds.user = System.getenv("CHEMCENTRAL_DB_USERNAME") ?: 'chemcentral'
        ds.password =  System.getenv("CHEMCENTRAL_DB_PASSWORD") ?:  'chemcentral'

        return ds;
    }
    
    def "1 create table"() {
        println "create table()"
        setup:
        
        try {
            db.execute 'drop table ' + DbFileService.DEMO_FILES_TABLE_NAME
        } catch (Exception e) { }// expected   
        DbFileService service = new DbFileService(dataSource)
        
        when:
        service.createTables()
        
        
        then:
        db.firstRow('select count(*) from ' + DbFileService.DEMO_FILES_TABLE_NAME)[0] == 0
        
    }
    
    
    def "2.1 add dataitem"() {
        println "add dataitem()"
        
        setup:
        DataItem data = new DataItem(name: 'test1', size: 100)
          
        when:
        DataItem neu = service.addDataItem(data, new ByteArrayInputStream('hello world!'.getBytes()))
        println "added data item with id ${neu.id}"
        item[0] = neu
        
        then:
        neu.id != null
        neu.name == 'test1'
        neu.size == 100
        neu.created != null
        neu.updated != null
        neu.loid != null
    }
    
    def "2.2 add dataitem tx"() {
        println "add dataitem tx()"
        
        setup:
        DataItem data = new DataItem(name: 'test1', size: 100)
        
          
        when:
        Connection con = service.connection
        con.autoCommit = false
        DataItem neu = service.addDataItem(con, data, new ByteArrayInputStream('hello world!'.getBytes()))
        con.commit()
        println "added data item with id ${neu.id}"
        item[0] = neu
        
        then:
        neu.id != null
        neu.name == 'test1'
        neu.size == 100
        neu.created != null
        neu.updated != null
        neu.loid != null
        
        cleanup:
        con?.close()
    }
    
    def "2.3 add update dataitem tx"() {
        println "add update dataitem tx()"
        
        setup:
        DataItem data = new DataItem(name: 'test1', size: 100)
        
          
        when:
        Connection con = service.connection
        con.autoCommit = false
        DataItem item1 = service.addDataItem(con, data, new ByteArrayInputStream('hello world!'.getBytes()))
        item1.size = 10000
        DataItem item2 = service.updateDataItem(con, item1);
        con.commit()
        
        
        then:
        item1 != null
        item2 != null
    }
    
    def "3 read large object"() {
        setup:
        def loid = item[0].loid
        Connection con = service.connection
        con.autoCommit = false
        InputStream is = service.createLargeObjectReader(con, loid)
        
        when:
        byte[] bytes = is.getBytes()
        
        then:
        new String(bytes) == 'hello world!'
        
        cleanup:
        is?.close()
        con?.close()
    }
    
    def "4 load data item"() {
        
        setup:
        def id = item[0].id
        
        when:
        DataItem data = service.loadDataItem(id)
        
        then:
        data.name == 'test1'
        data.created != null
        data.updated != null
        data.loid != null
    }
    
    def "5 load data items"() {
        
        when:
        def items = service.loadDataItems()
        
        then:
        items.size() > 0
    }
    
    def "6.1 update data item"() {
        
        setup:
        DataItem data = item[0]
        data.name = "I've changed"
        
        when:
        DataItem neu = service.updateDataItem(data)
        
        then:
        neu.name == "I've changed"
        neu.created != null
        neu.updated != null
        neu.created < neu.updated
        neu.loid != null
    }
    
    def "6.2 update data item tx"() {
        
        setup:
        DataItem data = item[0]
        data.name = "I've changed"
        
        when:
        Connection con = service.connection
        con.autoCommit = false
        DataItem neu = service.updateDataItem(con, data)
        con.commit()
        
        then:
        neu.name == "I've changed"
        neu.created != null
        neu.updated != null
        neu.created < neu.updated
        neu.loid != null
        
        cleanup:
        con?.close()
    }
    
    def "7 update file content"() {
        
        setup:
        DataItem data = item[0]
        Long oldLoid = data.loid
        def oldCreated = data.created
        def oldUpdated = data.updated
        InputStream is1 = new ByteArrayInputStream('another planet!'.getBytes())
        
        when:
        DataItem neu = service.updateDataItem(data, is1)
        Connection con = service.connection
        con.autoCommit = false
        InputStream is2 = service.createLargeObjectReader(con, neu.loid)
        byte[] bytes = is2.getBytes()
        
        then:
        neu.created != null
        neu.updated != null
        neu.created < neu.updated
        oldCreated == neu.created
        oldUpdated < neu.updated
        neu.loid != oldLoid
        new String(bytes) == 'another planet!'
        
        cleanup:
        is2?.close()
        con?.close()
    }
    
    def "8 delete data item"() {
        setup:
        def id = item[0].id
        DataItem data = service.loadDataItem(id)
        println "deleting item ${data.id} loid ${data.loid}"
        
        when:
        service.deleteDataItem(data)
        
        then:
        1 == 1
    }
}
