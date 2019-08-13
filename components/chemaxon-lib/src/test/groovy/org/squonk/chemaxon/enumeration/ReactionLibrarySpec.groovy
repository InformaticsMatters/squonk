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

package org.squonk.chemaxon.enumeration

import spock.lang.Shared
import spock.lang.Specification

import spock.lang.IgnoreIf

/**
 * Created by timbo on 05/07/16.
 */
@IgnoreIf({ System.getenv('CHEMAXON_LIBRARY_ABSENT') != null })
class ReactionLibrarySpec extends Specification {

    @Shared ReactionLibrary rxnlib = new ReactionLibrary("../../docker/deploy/images/chemservices/chemaxon_reaction_library.zip")

    void "read rxn names"() {

        when:
        def names = rxnlib.getReactionNames()
        //println '"' + names.join('",\n"') + '"'

        then:
        names.size() > 0
    }


    void "read mrv"() {

        when:
        def names = rxnlib.getReactionNames()
        String mrv = rxnlib.getReaction(names[0])
        //println mrv

        then:
        mrv.length() > 0
    }

}
