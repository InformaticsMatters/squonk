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

package org.squonk.camel.rdkit.processor

import org.RDKit.RDKFuncs
import org.squonk.types.MoleculeObject
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

import static org.squonk.rdkit.mol.EvaluatorDefinition.Function.LOGP

/**
 *
 * @author timbo
 */
class RDKitMoleculeProcessorSpec extends Specification {
    
    void "simple calc logp"() {
        
        setup:
        println "simple calc logp()"
        RDKitMoleculeProcessor p = new RDKitMoleculeProcessor()
        p.calculate(LOGP, 'logp')
        def mols = Stream.of(new MoleculeObject("C", "smiles"), new MoleculeObject("CC", "smiles"))
        
        when: 
        Stream<MoleculeObject> result = p.evaluate(null, mols, p.definitions, [:])
        def list = result.collect(Collectors.toList())
        
        then:
        list.size() == 2
        list[0].getValue("logp") != null
        list[1].getValue("logp") != null
    }

//    void "rdkit version"() {
//
//        when:
//        def ver =  RDKFuncs.getRdkitVersion()
//        println "version $ver"
//
//        then:
//        ver != null
//
//    }
	
}
