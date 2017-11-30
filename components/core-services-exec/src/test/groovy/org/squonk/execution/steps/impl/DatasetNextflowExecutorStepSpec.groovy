package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.core.NextflowServiceDescriptor
import org.squonk.core.ServiceDescriptorUtils
import org.squonk.dataset.Dataset
import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 02/08/17.
 */
class DatasetNextflowExecutorStepSpec extends Specification {

    static Dataset createDataset() {
        def mols = [
                new MoleculeObject('C', 'smiles', [idx: 0, a: 11, b: 'red', c: 7, d: 5]),
                new MoleculeObject('CC', 'smiles', [idx: 1, a: 23, b: 'blue', c: 5]),
                new MoleculeObject('CCC', 'smiles', [idx: 2, a: 7, b: 'green', c: 5, d: 7]),
                new MoleculeObject('CCCC', 'smiles', [idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
        return ds
    }

    static NextflowServiceDescriptor createServiceDescriptor(String path) {
        ServiceDescriptorUtils.readServiceDescriptor(path, NextflowServiceDescriptor.class)
    }

    static VariableManager createAndPrepareVariableManager() {
        def varman = new VariableManager(null, 1, 1)
        varman.putValue(
                new VariableKey(0, "output"),
                Dataset.class,
                createDataset())
        return varman
    }

    void "thick execute"() {

        def nsd = createServiceDescriptor("src/test/groovy/org/squonk/execution/steps/impl/nextflow1.nsd.yml")
        println "Creating executor " + nsd.serviceConfig.executorClassName
        def step = Class.forName(nsd.serviceConfig.executorClassName).newInstance()
        def varman = createAndPrepareVariableManager()
        def jobId = UUID.randomUUID().toString()
        step.configure(1, jobId, ['arg.message':'WTF Venus'],
                ["input": new VariableKey(0, "output")],
                [:],
                nsd)
        def context = new DefaultCamelContext()

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(1, "output"), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4
    }

    void "thin execute"() {

        def nsd = createServiceDescriptor("src/test/groovy/org/squonk/execution/steps/impl/nextflow2.nsd.yml")
        println "Creating executor " + nsd.serviceConfig.executorClassName
        def step = Class.forName(nsd.serviceConfig.executorClassName).newInstance()
        def varman = createAndPrepareVariableManager()
        def jobId = UUID.randomUUID().toString()
        step.configure(1, jobId, ['arg.message':'WTF Venus'],
                ["input": new VariableKey(0, "output")],
                [:],
                nsd)
        def context = new DefaultCamelContext()

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(1, "output"), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4
    }
}
