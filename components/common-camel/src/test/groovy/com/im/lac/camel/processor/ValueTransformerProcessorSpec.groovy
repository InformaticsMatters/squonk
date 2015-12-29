package com.im.lac.camel.processor

import com.im.lac.types.BasicObject
import org.squonk.dataset.Dataset
import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ValueTransformerProcessorSpec extends Specification {
    
    def objs = [
        new BasicObject([one:'1', two:'2.4']),
        new BasicObject([one:'2', two:'3.4'])
    ]
    CamelContext context = new DefaultCamelContext()
    Dataset ds = new Dataset(BasicObject.class, objs)
    
    void "basic convert types"() {
        
       
        ValueTransformerProcessor converter = new ValueTransformerProcessor()
        .convertValueType('one', Integer.class)
        .convertValueType('two', Float.class)
        
        when:
        converter.execute(context.getTypeConverter(), ds)    
        
        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') instanceof Integer
        ds.items[0].getValue('two') instanceof Float
        
    }
    
    void "basic convert names"() {
        
       
        ValueTransformerProcessor converter = new ValueTransformerProcessor()
        .convertValueName('one', 'three')
        
        when:
        converter.execute(context.getTypeConverter(), ds)    
        
        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') == null
        ds.items[0].getValue('two') == '2.4' 
        ds.items[0].getValue('three') == '1' 
    }
	
}

