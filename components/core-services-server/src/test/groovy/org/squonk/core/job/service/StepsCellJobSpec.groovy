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

package org.squonk.core.job.service

import org.squonk.io.IODescriptor
import org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition
import org.squonk.jobdef.JobStatus
import org.squonk.jobdef.StepsCellExecutorJobDefinition
import org.apache.camel.CamelContext
import org.squonk.client.JobStatusClient
import org.squonk.core.service.job.MemoryJobStatusClient
import org.squonk.core.service.job.StepsCellJob
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.impl.EchoStep
import org.squonk.notebook.api.VariableKey
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 06/01/16.
 */
class StepsCellJobSpec extends Specification {

    @Shared JobStatusClient jobStatusClient = new MemoryJobStatusClient()

    void "basic test"() {

        Long producer = 1
        StepDefinition steps = new StepDefinition(
                EchoStep.class.getName(),
                [:],
                [(new IODescriptor("input", "text/plain", String.class, null)):new VariableKey(producer, "input")],
                [(new IODescriptor("output", "text/plain", String.class, null)):"output"])

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(1, 1, 1, null, null, steps);
        int camelCounter = 0

        when:
        StepsCellJob job = new StepsCellJob(jobStatusClient, jobdef) {
            protected void startJob(CamelContext camelContext, String username) {
                camelCounter++
            }
        }
        JobStatus status = job.start(null, "username", 0)


        then:
        status.status == JobStatus.Status.RUNNING
        status.getJobId() != null
        job.jobstatusClient.list(null).size() == 1
        camelCounter == 1

    }

//    void "submit to queue"() {
//        MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials()
//        String url = rabbitmqCredentials.generateUrl(MQUEUE_JOB_STEPS_EXCHANGE_NAME,MQUEUE_JOB_STEPS_EXCHANGE_PARAMS)
//        println "Queue: " + url
//
//        Map<String,Object> headers = [:]
//        headers.put("rabbitmq.ROUTING_KEY", "jobs.steps")
//        headers.put("SquonkJobID", "ABCD1234")
//        headers.put("SquonkUsername", "nobody")
//
//        CamelContext context = new DefaultCamelContext()
//        context.start()
//
//        ProducerTemplate pt = context.createProducerTemplate()
//        String body = '''{"@class":"org.squonk.jobdef.ExecuteCellUsingStepsJobDefinition","notebookId":1,"cellName":"cell1","steps":[{"implementationClass":"org.squonk.execution.steps.impl.EchoStep","inputVariableMappings":{"input":{"producerName":"p","name":"input"}},"outputVariableMappings":{"output":"output"}}]}'''
//
//
//        when:
//        pt.sendBodyAndHeaders(url, body, headers)
//
//
//        then:
//        1 == 1
//
//        cleanup:
//        context?.stop()
//
//    }


}
