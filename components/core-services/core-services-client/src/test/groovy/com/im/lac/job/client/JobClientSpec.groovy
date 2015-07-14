package com.im.lac.job.client

import com.im.lac.dataset.DataItem
import com.im.lac.job.client.JobClient
import com.im.lac.job.jobdef.*
import java.util.stream.Stream
import java.util.stream.Collectors
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class JobClientSpec extends Specification {
    
    String url = "http://localhost/coreservices/rest/v1/jobs"
    
    void "1. test post do nothing job"() {
        
        setup:
        println "1. test post do nothing job()"
        def client = new JobClient(url)
        def jobdef = new DoNothingJobDefinition()
        
        
        when:
        def status = client.submitJob(jobdef)
        println "received status " + status
        
        then: 
        status != null
        status.status == JobStatus.Status.COMPLETED
    }
    
    void "2. list jobs"() {
        
        setup:
        println "2. list jobs()"
        def client = new JobClient(url)
        
        
        when:
        def statuses = client.getJobStatuses(1, null, null, null, null, null)
        println "received statuses " + statuses
        
        then: 
        statuses != null
        statuses.size() == 1
    }
    
     void "3. get job status"() {
        
        setup:
        println "3. get job status()"
        def client = new JobClient(url)
        
        
        when:
        def statuses = client.getJobStatuses(1, null, null, null, null, null)
        def status = client.getJobStatus(statuses[0].jobId)
        
        then: 
        status != null
        status.status == JobStatus.Status.COMPLETED
    }
    
}

