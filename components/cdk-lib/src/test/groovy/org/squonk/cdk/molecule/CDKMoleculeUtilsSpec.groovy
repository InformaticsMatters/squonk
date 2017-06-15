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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.squonk.cdk.molecule

import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.silent.Atom
import org.openscience.cdk.silent.AtomContainer
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CDKMoleculeUtilsSpec extends Specification {
    
    void "add implicit H"() {
        IAtomContainer methane = new AtomContainer()
        IAtom carbon = new Atom("C")
        methane.addAtom(carbon)
        
               
        when:
        CDKMoleculeUtils.initializeMolecule(methane)
        
        then:
        methane.atomCount == 1
        carbon.implicitHydrogenCount == 4
        
        
    }
	
}

