/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.dataset.transform.TransformDefinitions
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ValueTransformerStepSpec extends Specification {
    
    void "test write mols"() {
        
        DefaultCamelContext context = new DefaultCamelContext()
        
        def mols = [
            new MoleculeObject("C", "smiles", [num:"1",hello:'world']),
            new MoleculeObject("CC", "smiles", [num:"99",hello:'mars',foo:'bar']),
            new MoleculeObject("CCC", "smiles", [num:"100",hello:'mum'])
        ]
        Dataset input = new Dataset(MoleculeObject.class, mols)
        
        TransformDefinitions tdefs = new TransformDefinitions()
        .deleteField("foo")
        .renameField("hello", "goodbye")
        .convertField("num", Integer.class);
        
        ValueTransformerStep step = new ValueTransformerStep()
        step.configure("test write mols",
                [(ValueTransformerStep.OPTION_TRANSFORMS):tdefs],
                ValueTransformerStep.SERVICE_DESCRIPTOR,
                context, null
        )
        
        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input))
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.generateMetadata()
        List results = dataset.getItems()
        results.size() == 3 
        DatasetMetadata md = dataset.metadata
        md != null
        md.valueClassMappings.size() == 2
        md.valueClassMappings['num'] == Integer.class
        md.valueClassMappings['goodbye'] == String.class
        
    }

}