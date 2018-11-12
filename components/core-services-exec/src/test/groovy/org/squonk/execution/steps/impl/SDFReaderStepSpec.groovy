/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

import org.squonk.io.InputStreamDataSource
import org.squonk.io.SquonkDataSource
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class SDFReaderStepSpec extends Specification {

    void "test read sdf"() {

        SDFReaderStep step = new SDFReaderStep()
        String myFileName = "Kinase_inhibs.sdf.gz"
        FileInputStream is = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        SquonkDataSource input = new InputStreamDataSource("sdf", myFileName, CommonMimeTypes.MIME_TYPE_MDL_SDF, is, true)
        step.configure("test read sdf", [:], SDFReaderStep.SERVICE_DESCRIPTOR)

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), null)
        def dataset = resultsMap["output"]

        then:
        dataset != null
        dataset.items.size() == 36
        dataset.metadata.getProperties()['source'].contains(myFileName)

        cleanup:
        is.close()

    }


}

