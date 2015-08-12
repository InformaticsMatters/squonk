package com.im.lac.services.util

import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource

import com.im.lac.dataset.DataItem
import com.im.lac.dataset.Metadata
import com.im.lac.job.jobdef.JobStatus
import com.im.lac.services.dataset.service.*
import com.im.lac.services.job.Job

/**
 *
 * @author timbo
 */
class TestUtils {
    
    public final static String LAC_PASSWORD = 'lacrocks' 
    public final static String TEST_USERNAME = "testuser"
        
//    static List<Long> createTestData(DatasetHandler handler) {
//
//        def ids = []
//
//        ids << handler.createDataset('World', 'test0').id
//        ids << handler.createDataset(["one", "two", "three"],'test1').id
//        ids << handler.createDataset(["red", "yellow", "green", "blue"],'test2').id
//        ids << handler.createDataset(["banana", "pineapple", "orange", "apple", "pear"], 'test3').id
//        
//        return ids
//    }
    
    static DataSource createTestDataSource() {
        return Utils.createDataSource(null, null, 'squonk', "tester", LAC_PASSWORD)
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

