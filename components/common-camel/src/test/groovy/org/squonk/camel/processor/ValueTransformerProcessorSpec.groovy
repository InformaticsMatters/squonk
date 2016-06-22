package org.squonk.camel.processor

import org.squonk.types.BasicObject
import org.squonk.dataset.Dataset
import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.types.QualifiedValue
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

    void "basic transform values"() {


        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .transformValue('two', '2.4', '99.9')

        when:
        converter.execute(context.getTypeConverter(), ds)

        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') == '1'
        ds.items[0].getValue('two') == '99.9'
        ds.items[1].getValue('one') == '2'
        ds.items[1].getValue('two') == '3.4'

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
    }

    void "basic convert qualified value"() {

        Dataset ds = new Dataset(BasicObject.class, [
                new BasicObject([one:'1', two:'2.4']),
                new BasicObject([one:'<1', two:'<3.4'])
        ])


        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .convertValueType('one', QualifiedValue.class, Integer.class)
                .convertValueType('two', QualifiedValue.class, Float.class)

        when:
        converter.execute(context.getTypeConverter(), ds)

        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') instanceof QualifiedValue
        ds.items[0].getValue('two') instanceof QualifiedValue
        ds.items[1].getValue('one') instanceof QualifiedValue
        ds.items[1].getValue('two') instanceof QualifiedValue

        ds.items[0].getValue('one').value instanceof Integer
        ds.items[0].getValue('two').value instanceof Float
        ds.items[1].getValue('one').value instanceof Integer
        ds.items[1].getValue('two').value instanceof Float

        ds.items[0].getValue('one').qualifier == QualifiedValue.Qualifier.EQUALS
        ds.items[0].getValue('two').qualifier == QualifiedValue.Qualifier.EQUALS
        ds.items[1].getValue('one').qualifier == QualifiedValue.Qualifier.LESS_THAN
        ds.items[1].getValue('two').qualifier == QualifiedValue.Qualifier.LESS_THAN


    }

	
}

