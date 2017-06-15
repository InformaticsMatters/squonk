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

import org.squonk.dataset.Dataset
import org.squonk.dataset.ThinDatasetWrapper
import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 19/01/17.
 */
class ThinDatasetWrapperSpec extends Specification {

    void "fat make thin"() {

        def mols = [
                new MoleculeObject("smiles1", "smiles", [a:'foo']),
                new MoleculeObject("smiles2", "smiles", [a:'bar']),
                new MoleculeObject("smiles3", "smiles", [a:'baz']),
        ]
        def input = new Dataset(MoleculeObject.class, mols)
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, true)
        def thin = wrapper.prepareInput(input)

        when:
        def items = thin.items

        then:
        thin.type == MoleculeObject.class
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 0
        items[0].uuid == mols[0].uuid
    }


    void "enrich with results"() {

        def mols = [
                new MoleculeObject("smiles1", "smiles", [a:'foo']),
                new MoleculeObject("smiles2", "smiles", [a:'bar']),
                new MoleculeObject("smiles3", "smiles", [a:'baz']),
        ]
        def response = [
                new BasicObject(mols[0].uuid, [b:'venus']),
                new BasicObject(mols[1].uuid, [b:'mercury']),
                new BasicObject(mols[2].uuid, [a:'earth']),
        ]
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, true)
        def thin = wrapper.prepareInput(new Dataset(MoleculeObject.class, mols))
        thin.items // need to consume
        def results = wrapper.generateOutput(new Dataset(BasicObject.class, response))

        when:
        def items = results.items

        then:
        results.type == MoleculeObject.class
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 2 // a + b
        items[0].uuid == mols[0].uuid
        items[2].getValue('a') == 'earth'
    }

    void "enrich new structure"() {

        def mols = [
                new MoleculeObject("smiles1", "smiles"),
                new MoleculeObject("smiles2", "smiles"),
                new MoleculeObject("smiles3", "smiles"),
        ]
        def response = [
                new MoleculeObject(mols[0].uuid, "smiles11", "smiles", [a:'foo']),
                new MoleculeObject(mols[1].uuid, "smiles12", "smiles", [a:'bar']),
                new MoleculeObject(mols[2].uuid, "smiles13", "smiles", [a:'baz']),
        ]
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, false)
        def thin = wrapper.prepareInput(new Dataset(MoleculeObject.class, mols))
        thin.items // need to consume
        def results = wrapper.generateOutput(new Dataset(MoleculeObject.class, response))

        when:
        def items = results.items

        then:
        results.type == MoleculeObject.class
        items.size() == 3
        items[0] instanceof MoleculeObject
        items[0].values.size() == 1 // a
        items[0].uuid == mols[0].uuid
        items[0].source == 'smiles11'
        items[0].getValue('a') == 'foo'
    }

    void "filter"() {

        def mols = [
                new MoleculeObject("smiles1", "smiles", [a:'foo']),
                new MoleculeObject("smiles2", "smiles", [a:'bar']),
                new MoleculeObject("smiles3", "smiles", [a:'baz']),
        ]
        def response = [
                new BasicObject(mols[0].uuid),
                new BasicObject(mols[2].uuid),
        ]
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, true, true)
        def thin = wrapper.prepareInput(new Dataset(MoleculeObject.class, mols))
        thin.items // need to consume
        def results = wrapper.generateOutput(new Dataset(BasicObject.class, response))

        when:
        def items = results.items

        then:
        results.type == MoleculeObject.class
        items.size() == 2
        items[0] instanceof MoleculeObject
        items[0].values.size() == 1
        items[0].uuid == mols[0].uuid
        items[1].uuid == mols[2].uuid
        items[0].getValue('a') == 'foo'

    }

}
