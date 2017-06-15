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

package org.squonk.smartcyp

import org.openscience.cdk.Atom
import org.openscience.cdk.graph.ConnectivityChecker
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IMoleculeSet
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.MoleculeObject
import spock.lang.Specification

import java.util.stream.Collectors

/**
 * Created by timbo on 28/09/2016.
 */
class SMARTCypRunnerSpec extends Specification {

    void "execute smiles 10"() {
        Dataset dataset = Molecules.nci10Dataset()
        SMARTCypRunner runner = new SMARTCypRunner()

        when:
        def results = runner.execute(dataset.stream)
        def items = results.collect(Collectors.toList())
        def range = runner.getGeneralRange()
//        items.eachWithIndex { v, i ->
//            println "$i $v.source"
//            println v.values
//        }

        then:
        items.size() == 10
        range.getMinValue() < 100
        range.getMaxValue() > 0

    }


    void "execute molfile"() {
        Dataset dataset = Molecules.datasetFromSDF(Molecules.KINASE_INHIBS_SDF)
        SMARTCypRunner runner = new SMARTCypRunner()

        when:
        def results = runner.execute(dataset.stream)
        def items = results.collect(Collectors.toList())
//        items.eachWithIndex { v, i ->
//            println "$i $v.source"
//            println v.values
//        }

        then:
        items.size() == 36
    }

    void fragments() {

        def mols = [
                new MoleculeObject('CC1=CC(=O)C=CC1=O', 'smiles'), new MoleculeObject('C.CC1=CC(=O)C=CC1=O', 'smiles')
        ]
        def dataset = new Dataset(MoleculeObject.class, mols)

        SMARTCypRunner runner = new SMARTCypRunner()

        when:
        def results = runner.execute(dataset.stream)
        def items = results.collect(Collectors.toList())
//        items.eachWithIndex { v, i ->
//            println "$i $v.source"
//            println v.values
//        }

        then:
        items.size() == 2
        items[0].getValue('SMARTCyp_GEN').scores[0].atomIndex == items[1].getValue('SMARTCyp_GEN').scores[0].atomIndex - 1
        items[0].getValue('SMARTCyp_2D6').scores[0].atomIndex == items[1].getValue('SMARTCyp_2D6').scores[0].atomIndex - 1
        items[0].getValue('SMARTCyp_2C9').scores[0].atomIndex == items[1].getValue('SMARTCyp_2C9').scores[0].atomIndex - 1
    }

    void caffeine() {

        def mols = [
                new MoleculeObject(Molecules.caffeine.smiles, 'smiles')
        ]
        def dataset = new Dataset(MoleculeObject.class, mols)

        SMARTCypRunner runner = new SMARTCypRunner()

        when:
        def results = runner.execute(dataset.stream)
        def items = results.collect(Collectors.toList())
        items.eachWithIndex { v, i ->
            println "$i $v.source"
            println v.values
        }

        then:
        items.size() == 1
    }

}
