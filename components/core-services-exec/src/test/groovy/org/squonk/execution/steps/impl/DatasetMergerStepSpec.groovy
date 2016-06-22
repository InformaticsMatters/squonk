package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetMergerStepSpec extends Specification {
    
    def objs1() { [
            new BasicObject([id:1, a:"1", hello:'world']),
            new BasicObject([id:2,a:"99",hello:'mars',foo:'bar']),
            new BasicObject([id:3,a:"100",hello:'mum'])
    ]}
        
    def objs2() { [
        new BasicObject([id:2,b:"1",hello:'jupiter']),
        new BasicObject([id:3,b:"99",hello:'saturn',foo:'baz']),
        new BasicObject([id:4,b:"100",hello:'uranus'])
    ]}
    
    def objs3() { [
        new BasicObject([id:3,c:"11"]),
        new BasicObject([id:4,c:"999"]),
        new BasicObject([id:5,c:"100"])
    ]}
    
    def mols1() { [
            new MoleculeObject("C", "smiles", [id:1,c:"11"]),
            new MoleculeObject("CC", "smiles", [id:2, c:"999"]),
            new MoleculeObject("CCC", "smiles", [id:4,c:"100"])
    ]}

    Long producer = 1
    
    void "merge 2 datasets keep first"() {
        println "merge 2 datasets keep first()"
        
        DefaultCamelContext context = new DefaultCamelContext()

//        objs1.each {
//            println "objs1 " + it
//        }
//        objs2.each {
//            println "objs2 " + it
//        }

        Dataset ds1 = new Dataset(BasicObject.class, objs1())
        Dataset ds2 = new Dataset(BasicObject.class, objs2())
        
        
        VariableManager varman = new VariableManager(null,1,1);
        
        varman.putValue(
            new VariableKey(producer, "input1"),
            Dataset.class, 
            ds1)
        
        varman.putValue(
                new VariableKey(producer, "input2"),
                Dataset.class,
            ds2)
        
        DatasetMergerStep step = new DatasetMergerStep()
        step.configure(producer, "job1", [(DatasetMergerStep.OPTION_MERGE_FIELD_NAME):'id'],
                [(DatasetMergerStep.VAR_INPUT_1):new VariableKey(producer, "input1"), (DatasetMergerStep.VAR_INPUT_2):new VariableKey(producer, "input2")],
                [:])
        
        when:
        step.execute(varman, context)
        Dataset result = varman.getValue(new VariableKey(producer, DatasetMergerStep.VAR_OUTPUT), Dataset.class)
        
        then:
        result != null
        def items = result.items
        items.size() == 4
        BasicObject bo2 = items.find {
            it.getValue('id') == 2
        }
        // original value should be retained
        bo2.getValue('hello') == 'mars'
    }
    
    void "merge 2 datasets keep last"() {
        println "merge 2 datasets keep last()"
        
        DefaultCamelContext context = new DefaultCamelContext()
        
        Dataset ds1 = new Dataset(BasicObject.class, objs1())
        Dataset ds2 = new Dataset(BasicObject.class, objs2())
        
        
        VariableManager varman = new VariableManager(null,1,1);

        varman.putValue(
                new VariableKey(producer, "input1"),
                Dataset.class,
                ds1)

        varman.putValue(
                new VariableKey(producer, "input2"),
                Dataset.class,
                ds2)
        
        DatasetMergerStep step = new DatasetMergerStep()
        step.configure(producer, "job1",
                [(DatasetMergerStep.OPTION_MERGE_FIELD_NAME):'id', (DatasetMergerStep.OPTION_KEEP_FIRST):false],
                [(DatasetMergerStep.VAR_INPUT_1):new VariableKey(producer, "input1"), (DatasetMergerStep.VAR_INPUT_2):new VariableKey(producer, "input2")],
                [:])
        
        when:
        step.execute(varman, context)
        Dataset result = varman.getValue(new VariableKey(producer, DatasetMergerStep.VAR_OUTPUT), Dataset.class)
        
        then:
        result != null
        def items = result.items
        items.size() == 4
        BasicObject bo2 = items.find {
            it.getValue('id') == 2
        }
        // original value should be replaced
        bo2.getValue('hello') == 'jupiter'
    }
    
    
    void "merge 3 datasets"() {
        println "merge 3 datasets()"
        
        DefaultCamelContext context = new DefaultCamelContext()
        
        Dataset ds1 = new Dataset(BasicObject.class, objs1())
        Dataset ds2 = new Dataset(BasicObject.class, objs2())
        Dataset ds3 = new Dataset(BasicObject.class, objs3())
        
        VariableManager varman = new VariableManager(null,1,1);

        varman.putValue(
                new VariableKey(producer, "input1"),
                Dataset.class,
                ds1)

        varman.putValue(
                new VariableKey(producer, "input2"),
                Dataset.class,
                ds2)

        varman.putValue(
                new VariableKey(producer, "input3"),
                Dataset.class,
                ds3)
        
        DatasetMergerStep step = new DatasetMergerStep()
        step.configure(producer, "job1",
                [(DatasetMergerStep.OPTION_MERGE_FIELD_NAME):'id'],
                [(DatasetMergerStep.VAR_INPUT_1):new VariableKey(producer, "input1"), (DatasetMergerStep.VAR_INPUT_2):new VariableKey(producer, "input2"), (DatasetMergerStep.VAR_INPUT_3):new VariableKey(producer, "input3")],
                [:])
        
        when:
        step.execute(varman, context)
        Dataset result = varman.getValue(new VariableKey(producer, DatasetMergerStep.VAR_OUTPUT), Dataset.class)
        
        then:
        result != null
        def items = result.items
        items.size() == 5
    }
    
//    void "merge basic with molecules"() {
//        println "merge basic with molecules()"
//
//        DefaultCamelContext context = new DefaultCamelContext()
//
//        Dataset ds1 = new Dataset(BasicObject.class, objs1())
//        Dataset ds2 = new Dataset(MoleculeObject.class, mols1())
//
//
//        VariableManager varman = new VariableManager(new MemoryVariableClient(),1,1);
//
//        varman.putValue(
//                new VariableKey(producer, "input1"),
//                Dataset.class,
//                ds1)
//
//        varman.putValue(
//                new VariableKey(producer, "input2"),
//                Dataset.class,
//                ds2)
//
//        DatasetMergerStep step = new DatasetMergerStep()
//        step.configure(producer,
//                [(DatasetMergerStep.OPTION_MERGE_FIELD_NAME):'id'],
//                [(DatasetMergerStep.VAR_INPUT_1):new VariableKey(producer, "input1"), (DatasetMergerStep.VAR_INPUT_2):new VariableKey(producer, "input2")],
//                [:])
//
//        when:
//        step.execute(varman, context)
//        Dataset result = varman.getValue(new VariableKey(producer, DatasetMergerStep.VAR_OUTPUT), Dataset.class)
//
//        then:
//        result != null
//        def items = result.items
//        items.size() == 4
//        result.type == BasicObject.class
//    }
//
//    void "merge molecules with basic"() {
//        println "merge molecules with basic()"
//
//        DefaultCamelContext context = new DefaultCamelContext()
//
//        Dataset ds2 = new Dataset(BasicObject.class, objs1())
//        Dataset ds1 = new Dataset(MoleculeObject.class, mols1())
//
//
//        VariableManager varman = new VariableManager(new MemoryVariableClient(),1,1);
//
//        varman.putValue(
//                new VariableKey(producer, "input1"),
//                Dataset.class,
//                ds1)
//
//        varman.putValue(
//                new VariableKey(producer, "input2"),
//                Dataset.class,
//                ds2)
//
//        DatasetMergerStep step = new DatasetMergerStep()
//        step.configure(producer,
//                [(DatasetMergerStep.OPTION_MERGE_FIELD_NAME):'id'],
//                [(DatasetMergerStep.VAR_INPUT_1):new VariableKey(producer, "input1"), (DatasetMergerStep.VAR_INPUT_2):new VariableKey(producer, "input2")],
//                [:])
//
//        when:
//        step.execute(varman, context)
//        Dataset result = varman.getValue(new VariableKey(producer, DatasetMergerStep.VAR_OUTPUT), Dataset.class)
//
//        then:
//        result != null
//        def items = result.items
//        items.size() == 4
//        result.type == MoleculeObject.class
//    }
    
}