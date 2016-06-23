package org.squonk.core.util

import javax.sql.DataSource

import org.squonk.jobdef.JobStatus
import org.squonk.core.service.job.Job

/**
 *
 * @author timbo
 */
class TestUtils {

    public final static String TEST_USERNAME = 'squonkuser'
        

    static DataSource createTestDataSource() {
        return Utils.createDataSource()
    }
    
    static void waitForJobToComplete(Job job, long timeOutMillis) {
        long t0 = System.currentTimeMillis()
        long t1 = t0
        JobStatus status = job.getCurrentJobStatus()
        while (!(status.status == JobStatus.Status.COMPLETED || status.status == JobStatus.Status.ERROR)) {
            t1 = System.currentTimeMillis()
            if ((t1 - t0) > timeOutMillis) {
                break
            }
            sleep(100)
            status = job.getCurrentJobStatus()
        }
        //println "Completed in " + (t1 - t0) + " millis"
    }
    
	
}

