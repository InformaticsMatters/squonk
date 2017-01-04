package org.squonk.execution.steps.impl

import org.squonk.io.IODescriptor
import org.squonk.io.IODescriptors
import org.squonk.types.MoleculeObject
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import org.squonk.reader.SDFReader
import org.squonk.util.IOUtils
import spock.lang.Shared
import spock.lang.Specification

import java.util.zip.GZIPInputStream

/**
 *
 * @author timbo
 */
class MoleculeServiceFatExecutorStepDataSpec extends Specification {

    static String HOST_CDK_CALCULATORS = "http://" + IOUtils.getDockerGateway() + ":8092/chem-services-cdk-basic/rest/v1/calculators"
    static def inputs = [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[]
    static def outputs = [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[]


    Long producer = 1

    @Shared
    DefaultCamelContext context = new DefaultCamelContext()

    void setupSpec() {
        context.start()
    }

    void cleanupSpec() {
        context.stop()
    }

    MoleculeServiceFatExecutorStep createStep(Dataset ds) {

        def opts = [(MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT): HOST_CDK_CALCULATORS + '/logp']
        def inputMappings = [(MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET):new VariableKey(producer,"input")]
        def outputMappings = [:]

        MoleculeServiceFatExecutorStep step = new MoleculeServiceFatExecutorStep()
        step.configure(producer, "job1", opts, inputs, outputs, inputMappings, outputMappings)
        return step
    }
    
    void "test simple service"() {
               
        def mols = [
            new MoleculeObject("C", "smiles"),
            new MoleculeObject("CC", "smiles"),
            new MoleculeObject("CCC", "smiles")
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        
        VariableManager varman = new VariableManager(null, 1,1)
        varman.putValue(new VariableKey(producer,"input"), Dataset.class, ds)

        MoleculeServiceFatExecutorStep step = createStep(ds)
        
        
        when:
        step.execute(varman, context)
        
        then:
        def output = varman.getValue(new VariableKey(producer, MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET), Dataset.class)
        output instanceof Dataset
        def items = output.items
        items.size() == 3
        items[0].getValue('ALogP_CDK') != null
    }

    void "test kinase inhibs service"() {

        FileInputStream fis = new FileInputStream('../../data/testfiles/Kinase_inhibs.sdf.gz')
        SDFReader reader = new SDFReader(new GZIPInputStream(fis))
        Dataset ds = new Dataset(MoleculeObject.class, reader.asStream())

        VariableManager varman = new VariableManager(null,1,1)
        varman.putValue(new VariableKey(producer,"input"), Dataset.class, ds)

        MoleculeServiceFatExecutorStep step = createStep(ds)


        when:
        step.execute(varman, context)

        then:
        def output = varman.getValue(new VariableKey(producer, MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET), Dataset.class)
        output instanceof Dataset
        def items = output.items
        items.size() == 36
        items[0].getValue('ALogP_CDK') != null
    }

    void "test building blocks service"() {

        FileInputStream fis = new FileInputStream('../../data/testfiles/Building_blocks_GBP.sdf.gz')
        SDFReader reader = new SDFReader(new GZIPInputStream(fis))
        Dataset ds = new Dataset(MoleculeObject.class, reader.asStream())

        VariableManager varman = new VariableManager(null,1,1)
        varman.putValue(new VariableKey(producer,"input"), Dataset.class, ds)

        MoleculeServiceFatExecutorStep step = createStep(ds)


        when:
        step.execute(varman, context)

        then:
        def output = varman.getValue(new VariableKey(producer, MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET), Dataset.class)
        output instanceof Dataset
        long  size = output.getStream().count()
        size == 7003
    }
	
}

