package com.im.lac.dataset.client

import com.im.lac.dataset.DataItem
import java.util.stream.Stream
import java.util.stream.Collectors
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetClientSpec extends Specification {
    
    String url = "http://localhost/coreservices/rest/v1/datasets"
    //String url = "http://demos.informaticsmatters.com:8080/coreservices/rest/v1/datasets"
	
    void "test list items"() {
        setup:
        def client = new DatasetClient(url)
        
        when:
        Stream stream = client.getAll()
        def items = stream.collect(Collectors.toList())
        
        then: 
        items.size() > 0
    }
    
    void "test get data item"() {
        setup:
        def client = new DatasetClient(url)
        
        when:
        DataItem item1 = client.getAll().findFirst().get()
        println "received $item1"
        DataItem item2 = client.get(item1.id)
        
        
        then: 
        item2 != null
        item1.id == item2.id
    }
    
    void "test get content"() {
        setup:
        def client = new DatasetClient(url)
        
        when:
        DataItem item1 = client.getAll().findFirst().get()
        println "received $item1"
        Stream stream = client.getContentsAsObjects(item1)
        def items = stream.collect(Collectors.toList())
        
        then: 
        items != null
        items.size() > 0
    }
    
    void "test post content"() {
        setup:
        def client = new DatasetClient(url)
        String smiles = 'c1ccccc1\nc1ccncc1\nCC\nCCC'
        String name = 'Random smiles'
        
        when:
        DataItem item = client.create(name, new ByteArrayInputStream(smiles.bytes))
        println "received $item"
        
        then: 
        item != null
        item.getName() == name
        item.getId() > 0
        item.getMetadata().getSize() == 4
    }
    
    
    void "test delete content"() {
        setup:
        def client = new DatasetClient(url)
        String smiles = 'c1ccccc1\nc1ccncc1\nCC\nCCC'
        String name = 'Random smiles'
        
        when:
        DataItem item = client.create(name, new ByteArrayInputStream(smiles.bytes))
        println "created dataset ${item.id}"
        int resp = client.delete(item.id)
        
        then: 
        resp == 200
    }
    
}

