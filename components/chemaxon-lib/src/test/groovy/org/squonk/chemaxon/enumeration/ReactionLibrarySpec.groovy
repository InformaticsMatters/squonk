package org.squonk.chemaxon.enumeration

import spock.lang.Shared
import spock.lang.Specification


/**
 * Created by timbo on 05/07/16.
 */
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
