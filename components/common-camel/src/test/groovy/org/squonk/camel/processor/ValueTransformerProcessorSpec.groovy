package org.squonk.camel.processor

import org.squonk.dataset.DatasetMetadata
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
        new BasicObject([one:'1', two:'2.4', 'with space':99]),
        new BasicObject([one:'2', two:'3.4', 'with space':99])
    ]
    CamelContext context = new DefaultCamelContext()
    Dataset ds = new Dataset(BasicObject.class, objs, new DatasetMetadata(BasicObject.class))
    
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
        ds.items[0].getValue('with space') == 99
    }

    void "basic delete values"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('one', null)

        when:
        converter.execute(context.getTypeConverter(), ds)
        println ds.items[0].values

        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') == null
        ds.items[0].getValue('two') != null
    }

    void "delete values space"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('with space', null)

        when:
        converter.execute(context.getTypeConverter(), ds)
        println ds.items[0].values

        then:
        ds.items.size() == 2
        ds.items[0].getValue('with space') == null
    }

    void "conditional delete values"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('one', "two == '2.4'")

        when:
        converter.execute(context.getTypeConverter(), ds)
        println ds.items[0].values

        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') == null
        ds.items[0].getValue('two') != null
        ds.items[1].getValue('one') != null
        ds.items[1].getValue('two') != null
    }

    void "conditional delete values space"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('with space', "two == '2.4'")

        when:
        converter.execute(context.getTypeConverter(), ds)
        println ds.items[0].values

        then:
        ds.items.size() == 2
        ds.items[0].getValue('with space') == null
        ds.items[0].getValue('two') != null
        ds.items[1].getValue('with space') != null
        ds.items[1].getValue('two') != null
    }

    void "conditional delete values multiple"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('one', "two == '2.4'")
                .deleteValue('one', "1 == 2")

        when:
        converter.execute(context.getTypeConverter(), ds)
        println ds.items[0].values

        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') == null
        ds.items[0].getValue('two') != null
        ds.items[1].getValue('one') != null
        ds.items[1].getValue('two') != null
    }

    void "basic replace values"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .replaceValue('two', '2.4', '99.9')

        when:
        converter.execute(context.getTypeConverter(), ds)
        println ds.metadata.getFieldMetaProp('two', DatasetMetadata.PROP_HISTORY)

        then:
        ds.items.size() == 2
        ds.items[0].getValue('one') == '1'
        ds.items[0].getValue('two') == '99.9'
        ds.items[1].getValue('one') == '2'
        ds.items[1].getValue('two') == '3.4'
        ds.metadata.getFieldMetaProp('two', DatasetMetadata.PROP_HISTORY) != null
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

    void "convert names spaces"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .convertValueName('with space', 'three')

        when:
        converter.execute(context.getTypeConverter(), ds)

        then:
        ds.items.size() == 2
        ds.items[0].getValue('three') == 99
        ds.items[0].getValue('two') == '2.4'
    }


    void "basic convert qualified values"() {

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

    void "basic delete rows"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteRow(null)

        when:
        converter.execute(context.getTypeConverter(), ds)

        then:
        ds.items.size() == 0
    }


    void "conditional delete rows"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteRow("one == '1'")

        when:
        converter.execute(context.getTypeConverter(), ds)

        then:
        ds.items.size() == 1
    }

//    void "convert to molecule"() {
//
//        Dataset mols = new Dataset(BasicObject.class, [
//                new BasicObject([smiles:'C', one:'1', two:'2.4']),
//                new BasicObject([smiles:'CC', one:'2', two:'3.4'])
//        ])
//
//        ValueTransformerProcessor converter = new ValueTransformerProcessor()
//                .convertToMolecule("smiles", "smiles")
//
//        when:
//        converter.execute(context.getTypeConverter(), mols)
//
//        then:
//        mols.items.size() == 2
//        mols.items[0].class == MoleculeObject.class
//        mols.items[0].values.size == 2
//    }


    void "assign values"() {

        Dataset data = new Dataset(BasicObject.class, [
                new BasicObject([one:1, two:2.4, 'with space':10]),
                new BasicObject([one:2, two:3.4, 'with space':20])
                ])

        new ValueTransformerProcessor()
                .assignValue('foo', expression, condition, "fail")
                .execute(context.getTypeConverter(), data)

        expect:
        data.items[0].values.foo == rec0
        data.items[1].values.foo == rec1

        where:
        expression         | condition | rec0  | rec1
        "one + 10"         | null      | 11    | 12
        "one + two"        | null      | 3.4   | 5.4
        "one + 10"         | "two < 3" | 11    | null
        "one + two"        | "two < 3" | 3.4   | null
    }

    void "math functions"() {

        Dataset data = new Dataset(BasicObject.class, [
                new BasicObject([one:1, two:2.4]),
                new BasicObject([one:10, two:3.4])
        ])

        new ValueTransformerProcessor()
                .assignValue('foo', expression, condition, "fail")
                .execute(context.getTypeConverter(), data)

        expect:
        data.items[0].values.foo == rec0
        data.items[1].values.foo == rec1

        where:
        expression                | condition | rec0  | rec1
        "log10(one)"              | null      | 0     | 1
        "floor(two)"              | null      | 2     | 3
        "log10(one) + floor(two)" | null      | 2     | 4
    }

    void "assign values handle errors"() {

        Dataset data = new Dataset(BasicObject.class, [
                new BasicObject([one:1, two:'2.2']),
                new BasicObject([one:2, two:'<3.4']),
                new BasicObject([one:3, two:'5.4'])
        ])

        new ValueTransformerProcessor()
                .assignValue('foo', expression, condition, "continue")
                .execute(context.getTypeConverter(), data)

        expect:
        data.items[0].values.foo == rec0
        data.items[1].values.foo == rec1
        data.items[2].values.foo == rec2
        data.items[0].values.TransformErrors == null
        data.items[1].values.TransformErrors != err
        data.items[2].values.TransformErrors == null
        println data.items[1].values

        where:
        expression        | condition | rec0  | rec1 | rec2 | err
        "two.toFloat()"   | null      | 2.2f  | null | 5.4f | true
        "one.toInteger()" | null      | 1     | 2    | 3    | false
    }


}

