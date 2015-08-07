package com.im.lac.job.jobdef

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

import com.im.lac.job.jobdef.*

/**
 *
 * @author timbo
 */
class JobDefinitionJsonSpec extends Specification {
    
    void "AsyncLocalProcessDatasetJobDefinition"() {
        setup:
        println "AsyncLocalProcessDatasetJobDefinition()"
        ObjectMapper mapper = new ObjectMapper()
        def jobdef = new AsyncLocalProcessDatasetJobDefinition(
            "serviceId",
            "accessModeId",
            null,
            1,
            ProcessDatasetJobDefinition.DatasetMode.CREATE,
            null)
            

        
        when:
        def json = mapper.writeValueAsString(jobdef)
        println json
        def obj = mapper.readValue(json, JobDefinition.class)
        
        then:
        json != null
        obj != null
        obj instanceof AsyncLocalProcessDatasetJobDefinition
        obj.datasetId == 1
    }
    
    void "AsyncHttpProcessDatasetJobDefinition"() {
        setup:
        println "AsyncHttpProcessDatasetJobDefinition()"
        ObjectMapper mapper = new ObjectMapper()
        def jobdef = new AsyncHttpProcessDatasetJobDefinition(
            "serviceId",
            "accessModeId",
            null,
            1,
            ProcessDatasetJobDefinition.DatasetMode.CREATE,
            null)
        
        when:
        def json = mapper.writeValueAsString(jobdef)
        println json
        def obj = mapper.readValue(json, JobDefinition.class)
        
        then:
        json != null
        obj != null
        obj instanceof AsyncHttpProcessDatasetJobDefinition
        obj.datasetId == 1
    }
    
    void "SplitAndQueueProcessDatasetJobDefinition"() {
        setup:
        println "SplitAndQueueProcessDatasetJobDefinition()"
        ObjectMapper mapper = new ObjectMapper()
        def jobdef = new SplitAndQueueProcessDatasetJobDefinition(
            1,
            "foo",
            ProcessDatasetJobDefinition.DatasetMode.CREATE,
            String.class,
            null)
        
        when:
        def json = mapper.writeValueAsString(jobdef)
        println json
        def obj = mapper.readValue(json, JobDefinition.class)
        
        then:
        json != null
        obj != null
        obj instanceof SplitAndQueueProcessDatasetJobDefinition
        obj.datasetId == 1
    }
    
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

