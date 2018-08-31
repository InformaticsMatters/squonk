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

import org.squonk.dataset.Dataset
import org.squonk.io.FileDataSource
import org.squonk.util.CommonMimeTypes
import spock.lang.Specification

class DatasetHandlerSpec extends Specification {

    void "create from datasources"() {

        when:
        def data = new FileDataSource(null, null, new File("../../data/testfiles/Kinase_inhibs.json.gz"), true)
        def meta = new FileDataSource(null, null, new File("../../data/testfiles/Kinase_inhibs.metadata"), false)
        def handler = new DatasetHandler(MoleculeObject.class)
        Dataset value = handler.create(['data': data, 'metadata': meta])

        then:
        value != null
        value instanceof Dataset
        value.getType() == MoleculeObject.class
        value.items.size() == 36
    }

    void "create from inputstreams"() {

        when:
        def data = new FileInputStream("../../data/testfiles/Kinase_inhibs.json.gz")
        def meta = new FileInputStream("../../data/testfiles/Kinase_inhibs.metadata")
        def handler = new DatasetHandler(MoleculeObject.class)
        Dataset value = handler.create(CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON, MoleculeObject.class, ['data': data, 'metadata': meta])

        then:
        value != null
        value instanceof Dataset
        value.getType() == MoleculeObject.class
        value.items.size() == 36

        cleanup:
        data?.close()
        meta?.close()
    }
}
