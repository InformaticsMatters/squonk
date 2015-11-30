package com.squonk.rdkit.db

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

/**
 * Created by timbo on 30/11/2015.
 */
class SubstructureSearchSpec extends Specification {

    void "to/from json"() {

        SubstructureSearch ss = new SubstructureSearch(smarts: 'CC', chiral: true, limit: 100)

        ObjectMapper mapper = new ObjectMapper()

        when:
        String json = mapper.writeValueAsString(ss)
        println json
        def ss2 = mapper.readValue(json, StructureSearch.class)

        then:
        json.length() > 0
        ss2 != null
        ss2.limit == 100
        ss2.smarts == 'CC'
        ss2.chiral == true


    }
}
