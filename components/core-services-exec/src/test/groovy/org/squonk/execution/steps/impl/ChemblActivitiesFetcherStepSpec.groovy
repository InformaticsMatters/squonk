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

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class ChemblActivitiesFetcherStepSpec extends Specification {
	
    void "test fetch"() {
        
        ChemblActivitiesFetcherStep step = new ChemblActivitiesFetcherStep()
        Long producer = 1
        step.configure("test fetch",
                [(ChemblActivitiesFetcherStep.OPTION_ASSAY_ID):'CHEMBL864878'],
                ChemblActivitiesFetcherStep.SERVICE_DESCRIPTOR, null, null)
        
        when:
        def resultsMap = step.doExecute(null)
        def dataset = resultsMap["output"]
        
        then:
        dataset != null
        dataset.items.size() == 10
        
    }
}

