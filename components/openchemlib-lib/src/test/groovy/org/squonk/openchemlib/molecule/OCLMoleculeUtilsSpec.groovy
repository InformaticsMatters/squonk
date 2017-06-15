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

package org.squonk.openchemlib.molecule

import com.actelion.research.chem.SmilesParser
import com.actelion.research.chem.StereoMolecule
import org.squonk.data.Molecules
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream
import java.util.zip.GZIPInputStream

/**
 * Created by timbo on 05/04/16.
 */
class OCLMoleculeUtilsSpec extends Specification {

    void "parse smiles"() {

        when:
        StereoMolecule mol = OCLMoleculeUtils.importSmiles(Molecules.ethanol.smiles)

        then:
        mol != null
    }

    void "parse molfile"() {

        when:
        StereoMolecule mol = OCLMoleculeUtils.importMolfile(Molecules.ethanol.v2000)

        then:
        mol != null
    }

    void "guess format"() {

        when:
        StereoMolecule mol1 = OCLMoleculeUtils.importString(Molecules.ethanol.smiles, null)
        StereoMolecule mol2 = OCLMoleculeUtils.importString(Molecules.ethanol.v2000, null)

        then:
        mol1 != null
        mol2 != null
    }


    void "read tdt stream"() {

        Stream mols = Molecules.nci10000Molecules().stream().parallel()

        when:
        AtomicInteger total = new AtomicInteger(0)
        AtomicInteger fails = new AtomicInteger(0)
        long count = mols.map() { mo ->
            total.incrementAndGet()
            StereoMolecule mol
            try {
                mol = OCLMoleculeUtils.importSmiles(mo.source)
            } catch (Exception e) {
                fails.incrementAndGet()
                println e.getMessage()
            }
            return mol
        }.count()
        println "count=$count total=$total fails=$fails"

        then:
        total.get() == 10000
        fails.get() == 0

        cleanup:
        mols?.close()
    }
}
