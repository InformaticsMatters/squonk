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

package org.squonk.util

import org.squonk.types.MoleculeObject
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Created by timbo on 09/05/2016.
 */
class MoleculeObjectUtilsSpec extends Specification {

    void "dedup two different mols"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC']),
                new MoleculeObject('CCC', 'smiles', [canon: 'CCC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', [], [], []).collect(Collectors.toList())

        then:
        results.size() == 2
        results[0].values.size() == 0
        results[1].values.size() == 0
    }

    void "dedup two identical mols"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', [], [], []).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 0
    }

    void "dedup keep first, last, append, lose"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'first', b: 'first', c: 1, d: 'random']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'second', b: 'second', c: 2, d: 'noise'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 3
        results[0].getValue('a') == 'first'
        results[0].getValue('b') == 'second'
        results[0].getValue('c') instanceof List
        results[0].getValue('c').size() == 2
    }

    void "dedup first null"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'second', b: 'second', c: 2, d: 'noise'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 3
        results[0].getValue('a') == 'second'
        results[0].getValue('b') == 'second'
        results[0].getValue('c') instanceof List
        results[0].getValue('c').size() == 1
    }

    void "dedup second null"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'first', b: 'first', c: 1, d: 'random']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 3
        results[0].getValue('a') == 'first'
        results[0].getValue('b') == 'first'
        results[0].getValue('c') instanceof List
        results[0].getValue('c').size() == 1
    }

    void "dedup structure replaced"() {
        def mols = [
                new MoleculeObject('XX', 'smiles', [canon: 'CC', a: 'first', b: 'first', c: 1, d: 'random']),
                new MoleculeObject('XX', 'smiles', [canon: 'CC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].source == 'CC'
    }

    void "read dataset from files"() {
        when:
        Dataset dataset = MoleculeObjectUtils.readDatasetFromFiles("../../data/testfiles/input")

        then:
        dataset.getMetadata().size == 36
        dataset.items.size() == 36
    }

    void "write dataset"() {

        Dataset<MoleculeObject> dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        ByteArrayOutputStream outMeta = new ByteArrayOutputStream();
        Stream<MoleculeObject> stream = dataset.stream.peek() { MoleculeObject mo ->
            mo.putValue("hello", "world")
        }
        dataset.replaceStream(stream)

        when:
        MoleculeObjectUtils.writeDataset(dataset, outData, outMeta, false)
        byte[] byteData = outData.toByteArray()
        byte[] byteMeta = outMeta.toByteArray()

        then:
        byteData.length > 0
        byteMeta.length > 0

    }


}
