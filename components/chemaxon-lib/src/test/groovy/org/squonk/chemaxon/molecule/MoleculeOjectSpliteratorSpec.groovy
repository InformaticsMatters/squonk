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

import org.squonk.types.MoleculeObject
import java.util.stream.Stream
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeOjectSpliteratorSpec extends Specification {
    
    void "simple read"() {
        setup:
        FileInputStream file = new FileInputStream("../../data/testfiles/nci100.smiles")
        Stream<MoleculeObject> stream = new MoleculeObjectSpliterator(file).asStream(false)
        
        when:
        def count = stream.count()
        
        then:
        count == 100
        
        cleanup:
        file.close()
        
    }
	
}

