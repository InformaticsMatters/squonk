package com.im.lac.dataset.client

import com.im.lac.dataset.DataItem
import spock.lang.Shared

import java.util.stream.Stream
import java.util.stream.Collectors
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetClientSpec extends Specification {
    
    String username = "squonkuser"

    @Shared DatasetClient client = new DatasetClient()

    
    void "1. test post content"() {
        setup:
        String smiles = 'c1ccccc1\nc1ccncc1\nCC\nCCC'
        String name = 'Random smiles'
        
        when:
        DataItem item = client.create(username, name, new ByteArrayInputStream(smiles.bytes))
        println "received $item"
        
        then: 
        item != null
        item.getName() == name
        item.getId() > 0
        item.getMetadata().getSize() == 4
    }
	
    void "2. test list items"() {

        when:
        Stream stream = client.getAll(username)
        def items = stream.collect(Collectors.toList())
        
        then: 
        items.size() > 0
    }
    
    void "3. test get data item"() {

        when:
        DataItem item1 = client.getAll(username).findFirst().get()
        println "received $item1"
        DataItem item2 = client.get(username, item1.id)
        
        
        then: 
        item2 != null
        item1.id == item2.id
    }
    
    void "4. test get content"() {

        when:
        DataItem item1 = client.getAll(username).findFirst().get()
        println "received $item1"
        Stream stream = client.getContentsAsObjects(username, item1)
        def items = stream.collect(Collectors.toList())
        
        then: 
        items != null
        items.size() > 0
    }
      
    void "5. test delete content"() {
        setup:
        String smiles = 'c1ccccc1\nc1ccncc1\nCC\nCCC'
        String name = 'Random smiles'
        
        when:
        DataItem item = client.create(username, name, new ByteArrayInputStream(smiles.bytes))
        println "created dataset ${item.id}"
        int resp = client.delete(username, item.id)
        
        then: 
        resp == 200
    }
    
}

