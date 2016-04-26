package com.im.lac.job.jobdef

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

import com.im.lac.job.jobdef.*

/**
 *
 * @author timbo
 */
class JobDefinitionJsonSpec extends Specification {
    
    void "DoNothingJobDefinition"() {
        
        setup:
        println "DoNothingJobDefinition()"
        ObjectMapper mapper = new ObjectMapper()
        def jobdef = new DoNothingJobDefinition()
        
        when:
        def json = mapper.writeValueAsString(jobdef)
        println json
        def obj = mapper.readValue(json, JobDefinition.class)
        
        then:
        json != null
        obj != null
        obj instanceof DoNothingJobDefinition
    }
	
}
