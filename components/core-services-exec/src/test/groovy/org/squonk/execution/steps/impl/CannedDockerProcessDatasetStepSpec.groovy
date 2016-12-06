package org.squonk.execution.steps.impl

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.execution.steps.StepDefinitionConstants
import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

/**
 * Created by timbo on 13/09/16.
 */
class CannedDockerProcessDatasetStepSpec extends Specification {


    Long producer = 1

    def createDataset() {
        def mols = [
                new MoleculeObject('C', 'smiles', [idx: 0, a: 11, b: 'red',    c: 7, d: 5]),
                new MoleculeObject('CC', 'smiles', [idx: 1, a: 23, b: 'blue',   c: 5]),
                new MoleculeObject('CCC', 'smiles', [idx: 2, a: 7,  b: 'green',  c: 5, d: 7]),
                new MoleculeObject('CCCC', 'smiles', [idx: 3, a: 17, b: 'orange', c: 1, d: 3])
        ]

        Dataset ds = new Dataset(MoleculeObject.class, mols)
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

    def createStep(args) {
        CannedDockerProcessDatasetStep step = new CannedDockerProcessDatasetStep()
        step.configure(producer, "job1",
                args,
                [(StepDefinitionConstants.VARIABLE_INPUT_DATASET): new VariableKey(producer, "input")],
                [:])
        return step
    }


    void "simple execute using json"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        Map args = [dockerCommand: 'cp input.data.gz output.data.gz', dockerImage:'busybox']
        CannedDockerProcessDatasetStep step = createStep(args)

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, StepDefinitionConstants.VARIABLE_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4

    }

    void "execute using sdf"() {

        DefaultCamelContext context = new DefaultCamelContext()
        VariableManager varman = createVariableManager()
        Map args = [dockerCommand: 'cp input.sdf.gz output.sdf.gz', dockerImage:'busybox',
                    (StepDefinitionConstants.OPTION_MEDIA_TYPE_INPUT): CommonMimeTypes.MIME_TYPE_MDL_SDF,
                    (StepDefinitionConstants.OPTION_MEDIA_TYPE_OUTPUT): CommonMimeTypes.MIME_TYPE_MDL_SDF]
        CannedDockerProcessDatasetStep step = createStep(args)

        when:
        step.execute(varman, context)
        Dataset dataset = varman.getValue(new VariableKey(producer, StepDefinitionConstants.VARIABLE_OUTPUT_DATASET), Dataset.class)

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 4

    }
}