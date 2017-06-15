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

package org.squonk.chemaxon.molecule

import org.squonk.types.MoleculeObjectIterable
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeObjectFactorySpec extends Specification {
    
    def "read smiles"() {
        setup:
        File file = new File("../../data/testfiles/nci100.smiles")
        MoleculeObjectIterable impl = MoleculeObjectUtils.createIterable(file)
        
        when:
        def mols = impl.collect()
        
        then:
        mols.size() == 100
        mols.each { mo ->
            assert "smiles" == mo.format
        }
        
    }
    
    def "read sdf"() {
        setup:
        File file = new File("../../data/testfiles/dhfr_standardized.sdf.gz")
        MoleculeObjectIterable impl = MoleculeObjectUtils.createIterable(file)
        
        when:
        def mols = impl.collect()
        
        then:
        mols.size() == 756
        mols.each { mo ->
            assert mo.getSource() != null
            assert mo.getValue('set') != null
        }
        
    }
	
}

