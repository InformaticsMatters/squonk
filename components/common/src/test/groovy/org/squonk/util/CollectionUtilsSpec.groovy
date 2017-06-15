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

import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CollectionUtilsSpec extends Specification {

    
    void "enumeration"() {
        setup:
        List<String> mols1 = [new String("A"),new String("B"),new String("C"),new String("D")]
        List<String> mols2 = [new String("1"),new String("2"),new String("3")]
        List<String> mols3 = [new String("X"),new String("Y"),new String("Z")]
        
        
        
        when:
        def q = CollectionUtils.combinatorialIterator(100, mols1, mols2, mols3)
        def all = q.collect()
        
        then:
        all.size() == 36
        
    }
    
    
}

