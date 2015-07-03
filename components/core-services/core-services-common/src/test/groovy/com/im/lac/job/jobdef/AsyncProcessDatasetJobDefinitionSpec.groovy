package com.im.lac.job.jobdef

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class AsyncProcessDatasetJobDefinitionSpec extends Specification {
    
    void "test generate json"() {
        setup:
        def jobdef = new AsyncProcessDatasetJobDefinition(99,
            "some.where",
            DatasetJobDefinition.DatasetMode.UPDATE,
            String.class,
            "holy cow")
        
        ObjectMapper mapper = new ObjectMapper()
        
        when:
        String json = mapper.writeValueAsString(jobdef)
        println json
        
        then:
        json != null
        
    }
    
    void "test read json"() {
        setup:
        String json = '''{"datasetId":99,
"destination":"some.where",
"mode":"UPDATE",
"resultType":"java.lang.String",
"datasetName":"holy cow"}'''
        ObjectMapper mapper = new ObjectMapper()
        
        when:
        AsyncProcessDatasetJobDefinition jobdef = mapper.readValue(json, AsyncProcessDatasetJobDefinition.class)
        
        then:
        jobdef != null
        jobdef.datasetId == 99
        jobdef.resultType == String.class
        jobdef.mode == DatasetJobDefinition.DatasetMode.UPDATE
    }
    
     void "test read json no dataset name"() {
        setup:
        String json = '''{"datasetId":99,
"destination":"some.where",
"mode":"UPDATE",
"resultType":"java.lang.String"}'''
        ObjectMapper mapper = new ObjectMapper()
        
        when:
        AsyncProcessDatasetJobDefinition jobdef = mapper.readValue(json, AsyncProcessDatasetJobDefinition.class)
        
        then:
        jobdef != null
        jobdef.datasetId == 99
        jobdef.resultType == String.class
        jobdef.mode == DatasetJobDefinition.DatasetMode.UPDATE
        jobdef.datasetName == null
    }
	
}

