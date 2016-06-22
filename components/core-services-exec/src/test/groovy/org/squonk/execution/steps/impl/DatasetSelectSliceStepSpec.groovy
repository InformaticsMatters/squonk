package org.squonk.execution.steps.impl

import org.squonk.types.MoleculeObject
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetSelectSliceStepSpec extends Specification {

    Long producer = 1

    def createDataset() {
        def mols = []
        for (i in 1..100) {
            mols << new MoleculeObject("C", "smiles", [idx:i])
        }
        println "mols starting size = " + mols.size()
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    def createVariableManager() {
        VariableManager varman = new VariableManager(null,1,1);
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                createDataset())
        return varman
    }

    def createStep(skip, count) {
        DatasetSelectSliceStep step = new DatasetSelectSliceStep()
        step.configure(producer, "job1",
                [(DatasetSelectSliceStep.OPTION_SKIP):skip, (DatasetSelectSliceStep.OPTION_COUNT):count],
                [(DatasetSelectSliceStep.VAR_INPUT_DATASET):new VariableKey(producer, "input")],
                [:])
        return step
    }
    
    void "test skip and count"() {
        
        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        DatasetSelectSliceStep step = createStep(10,10)
        
        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, ValueTransformerStep.VAR_OUTPUT_DATASET), Dataset.class)
        
        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 10
        results[0].getValue("idx") == 11
    }

    void "test skip only"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        DatasetSelectSliceStep step = createStep(10,null)

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, ValueTransformerStep.VAR_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 90
        results[0].getValue("idx") == 11
    }

    void "test count only"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        DatasetSelectSliceStep step = createStep(null,10)

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, ValueTransformerStep.VAR_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 10
        results[0].getValue("idx") == 1
    }

}