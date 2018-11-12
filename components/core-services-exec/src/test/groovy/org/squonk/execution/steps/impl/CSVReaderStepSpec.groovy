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
class CSVReaderStepSpec extends Specification {

    static String CSV1 = '''\
field1,field2,field3
1,one,uno
2,two,duo
3,three,tres'''

    static String TAB1 = '''\
field1\tfield2\tfield3
1\tone\tuno
2\ttwo\tduo
3\tthree\ttres'''


    void "simple csv reader with header"() {

        InputStream is = new ByteArrayInputStream(CSV1.bytes)

        String myFileName = "myfile.csv"
        SquonkDataSource input = new InputStreamDataSource("csv", myFileName, CommonMimeTypes.MIME_TYPE_TEXT_CSV, is, false)

        CSVReaderStep step = new CSVReaderStep()
        step.configure("simple csv reader with header", [
                (CSVReaderStep.OPTION_FORMAT_TYPE)               : 'DEFAULT',
                (CSVReaderStep.OPTION_USE_HEADER_FOR_FIELD_NAMES): true,
                (CSVReaderStep.OPTION_SKIP_HEADER_LINE)          : true
        ],
                CSVReaderStep.SERVICE_DESCRIPTOR
        )

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), null)
        def dataset = resultsMap["output"]

        then:
        dataset != null
        def items = dataset.items
        items.size() == 3
        items[0].values.size() == 3
        dataset.metadata.getProperties()['source'].contains(myFileName)
    }

    void "simple tab reader without header"() {

        InputStream is = new ByteArrayInputStream(TAB1.bytes)
        String myFileName = "myfile.tab"
        SquonkDataSource input = new InputStreamDataSource("tab", myFileName, CommonMimeTypes.MIME_TYPE_TEXT_CSV, is, false)

        CSVReaderStep step = new CSVReaderStep()

        step.configure("simple tab reader without header",
                [(CSVReaderStep.OPTION_FORMAT_TYPE): 'TDF'],
                CSVReaderStep.SERVICE_DESCRIPTOR)

        when:
        def resultsMap = step.doExecute(Collections.singletonMap("input", input), null)
        def dataset = resultsMap["output"]

        then:
        dataset != null
        def items = dataset.items
        items.size() == 4
        items[0].values.size() == 3
    }

}

