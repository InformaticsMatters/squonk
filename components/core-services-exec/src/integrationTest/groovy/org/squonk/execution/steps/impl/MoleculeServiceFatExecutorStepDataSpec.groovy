/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.execution.steps.impl

import org.squonk.core.HttpServiceDescriptor
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

        def inputMappings = ["input":new VariableKey(producer,"input")]
        def outputMappings = [:]

        HttpServiceDescriptor sd = new HttpServiceDescriptor("id.http", "name", "desc", null, null, null, null, null,
                [IODescriptors.createMoleculeObjectDataset("input")] as IODescriptor[],
                [IODescriptors.createMoleculeObjectDataset("output")] as IODescriptor[],
                null, "executor", HOST_CDK_CALCULATORS + '/logp')


        MoleculeServiceFatExecutorStep step = new MoleculeServiceFatExecutorStep()
        step.configure(producer, "job1", null, inputMappings, outputMappings, sd)
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
        def output = varman.getValue(new VariableKey(producer, "output"), Dataset.class)
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
        def output = varman.getValue(new VariableKey(producer, "output"), Dataset.class)
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
        def output = varman.getValue(new VariableKey(producer, "output"), Dataset.class)
        output instanceof Dataset
        long  size = output.getStream().count()
        size == 7003
    }
	
}

