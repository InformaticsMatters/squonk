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

package org.squonk.rdkit.io

import org.squonk.types.MoleculeObject
import spock.lang.Specification
import static org.squonk.rdkit.io.RDKitMoleculeIOUtils.FragmentMode.*

/**
 * Created by timbo on 07/05/16.
 */
class RDKitMoleculeIOUtilsSpec extends Specification {

    void "canonical smiles"() {

        expect:

        RDKitMoleculeIOUtils.generateCanonicalSmiles(new MoleculeObject(input, "smiles"), mode) == can

        where:
        input | mode | can
        'C1=CC=CN=C1' | WHOLE_MOLECULE | 'c1ccncc1'
        'C1=CC=CN=C1' | BIGGEST_BY_ATOM_COUNT | 'c1ccncc1'
        'C1=CC=CN=C1' | BIGGEST_BY_HEAVY_ATOM_COUNT | 'c1ccncc1'
        'C1=CC=CN=C1' | BIGGEST_BY_MOLWEIGHT | 'c1ccncc1'
        'BrC(Br)Br.C1=CC=NC=C1' | WHOLE_MOLECULE | 'BrC(Br)Br.c1ccncc1'
        'BrC(Br)Br.C1=CC=NC=C1' | BIGGEST_BY_ATOM_COUNT | 'c1ccncc1'
        'BrC(Br)Br.C1=CC=NC=C1' | BIGGEST_BY_HEAVY_ATOM_COUNT | 'c1ccncc1'
        'BrC(Br)Br.C1=CC=NC=C1' | BIGGEST_BY_MOLWEIGHT | 'BrC(Br)Br'
    }
}
