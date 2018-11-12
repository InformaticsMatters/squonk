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

package org.squonk.camel.typeConverters

import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.io.FileDataSource
import org.squonk.types.SDFile
import spock.lang.Specification

/**
 * Created by timbo on 24/03/2016.
 */
class MoleculeStreamTypeConverterSpec extends Specification {


    void "sdf to dataset"() {

        def data = new FileDataSource(null, null, new File("../../data/testfiles/Kinase_inhibs.sdf.gz"), true)
        SDFile sdf = new SDFile(data)

        when:
        MoleculeObjectDataset ds = MoleculeStreamTypeConverter.convertSDFileToMoleculeObjectDataset(sdf, null)

        then:
        ds != null
        ds.items.size() == 36
    }

}
