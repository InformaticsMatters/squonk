package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.execution.variable.VariableManager
import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class DatasetMoleculesFromFieldStepSpec extends Specification {

    Long producer = 1

    def createDataset() {
        def bos = []
        int count = 1
        for (i in 1..10) {
            BasicObject bo = new BasicObject();
            if (i % 2 == 0) {
                MoleculeObject[] mols = new MoleculeObject[5]
                for (j in 0..4) {
                    mols[j] = new MoleculeObject("C" * count++, "smiles", [idx: count])
                }
                bo.putValue("mols", mols)
            }
            bos << bo
        }
        println "bos starting size = " + bos.size()
        Dataset ds = new Dataset(BasicObject.class, bos)
        return ds
    }

    def createVariableManager() {
        VariableManager varman = new VariableManager(null, 1, 1);
        varman.putValue(
                new VariableKey(producer, "input"),
                Dataset.class,
                createDataset())
        return varman
    }

    def createStep(field) {
        DatasetMoleculesFromFieldStep step = new DatasetMoleculesFromFieldStep()
        step.configure(producer, "job1",
                [(DatasetMoleculesFromFieldStep.OPTION_MOLECULES_FIELD): field],
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                ["input": new VariableKey(producer, "input")],
                [:])
        return step
    }

    void "get 25 mols"() {

        def context = new DefaultCamelContext()
        def varman = createVariableManager()
        def step = createStep("mols")

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, "output"), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        def results = dataset.getItems()
        results.size() == 25
        dataset.metadata.size == 25
        results[24].source.length() == 25
    }

}