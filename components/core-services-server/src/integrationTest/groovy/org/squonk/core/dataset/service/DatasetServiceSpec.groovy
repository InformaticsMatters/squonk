package org.squonk.core.dataset.service

import org.squonk.core.util.Fruit
import org.squonk.core.util.TestUtils
import org.squonk.core.util.Utils
import spock.lang.Shared
import spock.lang.Specification
import com.im.lac.dataset.DataItem
import com.im.lac.dataset.Metadata
import com.im.lac.services.util.*

/**
 *
 * @author timbo
 */
class DatasetServiceSpec extends Specification {
    
    
    
    @Shared DatasetService service = new DatasetService(TestUtils.createTestDataSource())
    @Shared DataItem[] item = new DataItem[1] 
    
    
    // run before the first feature method
    def setupSpec() {
//        try {
//            service.dropTables()
//        } catch (Exception e) { }// expected   
//        service.createTables()
        service.deleteDataForUser(TestUtils.TEST_USERNAME)
    }
    
    // run after the last feature method
    def cleanupSpec() {
        //service.dropTables()
    }
    
    
    def "1.1 add dataitem"() {
        println "add dataitem()"
        
        setup:
        DataItem data = new DataItem(name: 'test1')
          
        when:
        DataItem neu = service.addDataItem(TestUtils.TEST_USERNAME, data, new ByteArrayInputStream('hello world!'.getBytes()))
        
        println "added data item with id ${neu.id}"
        item[0] = neu
        
        then:
        neu.id != null
        neu.name == 'test1'
        neu.created != null
        neu.updated != null
        neu.loid != null
    }
    
    def "1.2 add update dataitem"() {
        println "add updateStatus dataitem()"
        
        setup:
        DataItem data = new DataItem(name: 'test1')
        
        when:
        DataItem item1
        DataItem item2
        service.doInTransactionWithResult(DataItem.class) { sql ->
            item1 = service.addDataItem(sql, TestUtils.TEST_USERNAME, data, new ByteArrayInputStream('hello world!'.getBytes()))
            item2 = service.updateDataItem(sql, TestUtils.TEST_USERNAME, item1);
        }
        
        
        then:
        item1 != null
        item2 != null
    }
    
    def "2 read large object"() {
        setup:
        def loid = item[0].loid
           
        when:
        byte[] bytes
        service.doInTransaction() { sql ->
            InputStream is = service.createLargeObjectReader(sql, loid)
            bytes = is.getBytes()
            is.close()
        }
         
        then:
        new String(bytes) == 'hello world!'
    }
    
    def "3 get data item"() {
        
        setup:
        def id = item[0].id
        
        when:
        DataItem data = service.getDataItem(TestUtils.TEST_USERNAME, id)
        
        then:
        data.name == 'test1'
        data.created != null
        data.updated != null
        data.loid != null
    }
    
    def "4 get data items"() {
        
        when:
        def items = service.getDataItems(TestUtils.TEST_USERNAME)
        
        then:
        items.size() > 0
    }
    
    def "5.1 update data item"() {
        
        setup:
        DataItem data = item[0]
        data.name = "I've changed"
        
        when:
        DataItem neu = service.updateDataItem(TestUtils.TEST_USERNAME, data)
        
        then:
        neu.name == "I've changed"
        neu.created != null
        neu.updated != null
        neu.created < neu.updated
        neu.loid != null
    }
    
    
    def "6 update file content"() {
        
        setup:
        DataItem data = item[0]
        Long oldLoid = data.loid
        def oldCreated = data.created
        def oldUpdated = data.updated
        InputStream is1 = new ByteArrayInputStream('another planet!'.getBytes())
        
        when:
        DataItem neu = service.updateDataItem(TestUtils.TEST_USERNAME, data, is1)
        
        byte[] bytes
        service.doInTransaction() { sql ->
            InputStream is2 = service.createLargeObjectReader(sql, neu.loid)
            bytes = is2.getBytes()
            is2.close()
        }
                
        then:
        neu.created != null
        neu.updated != null
        neu.created < neu.updated
        oldCreated == neu.created
        oldUpdated < neu.updated
        neu.loid != oldLoid
        new String(bytes) == 'another planet!'
        
    }
    
    def "7 delete data item"() {
        setup:
        def id = item[0].id
        DataItem data = service.getDataItem(TestUtils.TEST_USERNAME, id)
        println "deleting item ${data.id} loid ${data.loid}"
        
        when:
        service.deleteDataItem(data)
        
        
        then:
        1 == 1
    }
    
    
    def "8 write json"() {
        setup:
        def fruit = new Fruit(type:'banana', colour:'yellow')
        def json = Utils.toJson(fruit)
        def meta = new Metadata(Fruit.class.getName(), Metadata.Type.ITEM, 1)
        
        when:
        DataItem di = service.addDataItem(TestUtils.TEST_USERNAME, new DataItem(name:'banana', metadata: meta), new ByteArrayInputStream(json.bytes))
        item[0] = di
        
        then:
        di.name == 'banana'
        di.id > 0
        di.loid > 0
        
    }
    
    def "9 read json"() {
        
        when:
        DataItem di = service.getDataItem(TestUtils.TEST_USERNAME, item[0].id)
        Fruit fruit = service.doInTransactionWithResult(Fruit.class) { sql ->
            InputStream is = service.createLargeObjectReader(sql, di.loid)
            return Utils.fromJson(is, Fruit.class)
        }
        
        then:
        di != null
        di.metadata != null
        fruit != null
        fruit.type == 'banana'
        fruit.colour == 'yellow'
    }
}
