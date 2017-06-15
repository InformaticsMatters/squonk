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

