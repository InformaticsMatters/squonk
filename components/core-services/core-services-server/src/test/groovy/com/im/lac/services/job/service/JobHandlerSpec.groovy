package com.im.lac.services.job.service

import com.im.lac.job.jobdef.JobStatus
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class JobHandlerSpec extends Specification {
    
    void "test get statuses"() {
        
        setup:
        JobHandler h = new JobHandler()
        JobStore store = new SimpleJobStore()
        Date now = new Date()
        store.putJob(new TestJob(new JobStatus("jobId1", JobStatus.Status.COMPLETED, 0, 0, 0, now, now, null, null)))
        
        when:
        def results = h.getJobStatuses(store)
        
        then:
        results.size() == 1
    }
    
    void "test purge old"() {
        
        setup:
        JobHandler h = new JobHandler()
        JobStore store = new SimpleJobStore()
        Date now = new Date()
        Date notsoold = new Date(now.time - (30 * 1000)) // 30s ago
        Date veryold = new Date(1)
        store.putJob(new TestJob(new JobStatus("jobId1", JobStatus.Status.COMPLETED, 0, 0, 0, now, now, null, null)))
        store.putJob(new TestJob(new JobStatus("jobId2", JobStatus.Status.COMPLETED, 0, 0, 0, notsoold, now, null, null)))
        store.putJob(new TestJob(new JobStatus("jobId3", JobStatus.Status.COMPLETED, 0, 0, 0, veryold, now, null, null)))
        
        when:
        def results1 = h.getJobStatuses(store)
        def results2 = h.getJobStatuses(store)
        
        then:
        results1.size() == 2
        results2.size() == 2
    }
    
    
    class TestJob extends AbstractDatasetServiceJob {
        
        TestJob(JobStatus status) {
            super(status)
        }
        
        Class getDefaultAdapterClass() {
            return null
        }
        
    }
	
}

