package org.squonk.camel.processor

import org.squonk.dataset.DatasetMetadata
import org.squonk.types.BasicObject
import org.squonk.dataset.Dataset
import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.types.MoleculeObject
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
        def nue = converter.execute(context.getTypeConverter(), ds)
        
        then:
        nue.items.size() == 2
        nue.items[0].getValue('one') instanceof Integer
        nue.items[0].getValue('two') instanceof Float
        nue.items[0].getValue('with space') == 99
    }

    void "basic delete values"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('one', null)

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)
        //println nue.items[0].values

        then:
        nue.items.size() == 2
        nue.items[0].getValue('one') == null
        nue.items[0].getValue('two') != null
    }

    void "delete values space"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('with space', null)

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)
        //println nue.items[0].values

        then:
        nue.items.size() == 2
        nue.items[0].getValue('with space') == null
    }

    void "conditional delete values"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('one', "two == '2.4'")

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)
        //println nue.items[0].values

        then:
        nue.items.size() == 2
        nue.items[0].getValue('one') == null
        nue.items[0].getValue('two') != null
        nue.items[1].getValue('one') != null
        nue.items[1].getValue('two') != null
    }

    void "conditional delete values space"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('with space', "two == '2.4'")

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)
        //println nue.items[0].values

        then:
        nue.items.size() == 2
        nue.items[0].getValue('with space') == null
        nue.items[0].getValue('two') != null
        nue.items[1].getValue('with space') != null
        nue.items[1].getValue('two') != null
    }

    void "conditional delete values multiple"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteValue('one', "two == '2.4'")
                .deleteValue('one', "1 == 2")

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)
        //println nue.items[0].values

        then:
        nue.items.size() == 2
        nue.items[0].getValue('one') == null
        nue.items[0].getValue('two') != null
        nue.items[1].getValue('one') != null
        nue.items[1].getValue('two') != null
    }

    void "basic replace values"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .replaceValue('two', '2.4', '99.9')

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)
        //println nue.metadata.getFieldMetaProp('two', DatasetMetadata.PROP_HISTORY)

        then:
        nue.items.size() == 2
        nue.items[0].getValue('one') == '1'
        nue.items[0].getValue('two') == '99.9'
        nue.items[1].getValue('one') == '2'
        nue.items[1].getValue('two') == '3.4'
        nue.metadata.getFieldMetaProp('two', DatasetMetadata.PROP_HISTORY) != null
    }

    void "basic convert names"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
        .convertValueName('one', 'three')
        
        when:
        def nue = converter.execute(context.getTypeConverter(), ds)
        
        then:
        nue.items.size() == 2
        nue.items[0].getValue('one') == null
        nue.items[0].getValue('two') == '2.4'
    }

    void "convert names spaces"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .convertValueName('with space', 'three')

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)

        then:
        nue.items.size() == 2
        nue.items[0].getValue('three') == 99
        nue.items[0].getValue('two') == '2.4'
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
        def nue = converter.execute(context.getTypeConverter(), ds)

        then:
        nue.items.size() == 2
        nue.items[0].getValue('one') instanceof QualifiedValue
        nue.items[0].getValue('two') instanceof QualifiedValue
        nue.items[1].getValue('one') instanceof QualifiedValue
        nue.items[1].getValue('two') instanceof QualifiedValue

        nue.items[0].getValue('one').value instanceof Integer
        nue.items[0].getValue('two').value instanceof Float
        nue.items[1].getValue('one').value instanceof Integer
        nue.items[1].getValue('two').value instanceof Float

        nue.items[0].getValue('one').qualifier == QualifiedValue.Qualifier.EQUALS
        nue.items[0].getValue('two').qualifier == QualifiedValue.Qualifier.EQUALS
        nue.items[1].getValue('one').qualifier == QualifiedValue.Qualifier.LESS_THAN
        nue.items[1].getValue('two').qualifier == QualifiedValue.Qualifier.LESS_THAN


    }

    void "basic delete rows"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteRow(null)

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)

        then:
        nue.items.size() == 0
    }


    void "conditional delete rows"() {

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .deleteRow("one == '1'")

        when:
        def nue = converter.execute(context.getTypeConverter(), ds)

        then:
        nue.items.size() == 1
        nue.items.size() == 1
    }

    void "convert to molecule"() {

        DatasetMetadata meta = new DatasetMetadata(BasicObject.class, null, null, -1, [token:'property'])
        meta.putFieldMetaProp('one', 'foo' , 'bar')
        Dataset mols = new Dataset(BasicObject.class, [
                new BasicObject([smiles:'C', one:'1', two:'2.4']),
                new BasicObject([smiles:'CC', one:'2', two:'3.4'])
        ], meta)

        ValueTransformerProcessor converter = new ValueTransformerProcessor()
                .convertToMolecule("smiles", "smiles")

        when:
        def nue = converter.execute(context.getTypeConverter(), mols)

        then:
        nue.items.size() == 2
        nue.items[0].class == MoleculeObject.class
        nue.items[0].values.size() == 2
        nue.metadata.getFieldMetaProp('one','foo') == 'bar'
        nue.metadata.getProperty('token') == 'property'
    }


    void "assign values"() {

        Dataset data = new Dataset(BasicObject.class, [
                new BasicObject([one:1, two:2.4, 'with space':10]),
                new BasicObject([one:2, two:3.4, 'with space':20])
                ])

        def nue = new ValueTransformerProcessor()
                .assignValue('foo', expression, condition, "fail")
                .execute(context.getTypeConverter(), data)

        expect:
        nue.items[0].values.foo == rec0
        nue.items[1].values.foo == rec1

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

        def nue = new ValueTransformerProcessor()
                .assignValue('foo', expression, condition, "fail")
                .execute(context.getTypeConverter(), data)

        expect:
        nue.items[0].values.foo == rec0
        nue.items[1].values.foo == rec1

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

        def nue = new ValueTransformerProcessor()
                .assignValue('foo', expression, condition, "continue")
                .execute(context.getTypeConverter(), data)

        expect:
        nue.items[0].values.foo == rec0
        nue.items[1].values.foo == rec1
        nue.items[2].values.foo == rec2
        nue.items[0].values.TransformErrors == null
        nue.items[1].values.TransformErrors != err
        nue.items[2].values.TransformErrors == null

        where:
        expression        | condition | rec0  | rec1 | rec2 | err
        "two.toFloat()"   | null      | 2.2f  | null | 5.4f | true
        "one.toInteger()" | null      | 1     | 2    | 3    | false
    }


}

