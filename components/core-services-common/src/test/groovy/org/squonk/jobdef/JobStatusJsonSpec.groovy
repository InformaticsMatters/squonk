package org.squonk.jobdef

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class JobStatusJsonSpec extends Specification {
    
    void "JobStatus"() {
        setup:
        println "JobStatus()"
        ObjectMapper mapper = new ObjectMapper()
        def status = new JobStatus(
            'jobone',
            'nobody',
            JobStatus.Status.COMPLETED,
            0,
            0,
            0,
            new Date(),
            new Date(),
            new DoNothingJobDefinition(),
            null)
        
        when:
        def json = mapper.writeValueAsString(status)
        println json
        def obj = mapper.readValue(json, JobStatus.class)
        
        then:
        json != null
        obj != null
        obj instanceof JobStatus
        obj.jobId == 'jobone'
    }
	
}

