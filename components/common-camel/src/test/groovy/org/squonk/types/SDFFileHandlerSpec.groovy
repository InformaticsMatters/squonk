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
package org.squonk.types

import org.squonk.io.FileDataSource
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

class SDFFileHandlerSpec extends Specification {

    void "create from datasource"() {

        when:
        def data = new FileDataSource(null, null, new File("../../data/testfiles/Kinase_inhibs.sdf.gz"), true)
        SDFileHandler handler = new SDFileHandler()
        def value = handler.create(data)

        then:
        value != null
        value instanceof SDFile
    }

    void "create from inputstream"() {

        when:
        def data = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        SDFileHandler handler = new SDFileHandler()
        def value = handler.create(CommonMimeTypes.MIME_TYPE_MDL_SDF, null, ["data": data])

        then:
        value != null
        value instanceof SDFile

        cleanup:
        data?.close()
    }
}
