/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core.util

import org.squonk.config.SquonkServerConfig

import javax.sql.DataSource

import org.squonk.jobdef.JobStatus
import org.squonk.core.service.job.Job

/**
 *
 * @author timbo
 */
class TestUtils {

    public final static String TEST_USERNAME = 'squonkuser'
        

    static DataSource createTestSquonkDataSource() {
        // TODO replace localhost and port with a lookup - docker host may not be on localhost in some dev environments?
        SquonkServerConfig.createDataSource("localhost", 5432, "squonk", "squonk", "squonk")
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

