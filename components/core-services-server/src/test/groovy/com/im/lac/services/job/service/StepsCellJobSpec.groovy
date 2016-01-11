package com.im.lac.services.job.service

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition
import com.im.lac.job.jobdef.JobStatus
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition
import com.im.lac.services.job.dao.MemoryJobStatusClient
import org.apache.camel.CamelContext
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.impl.EchoStep
import org.squonk.notebook.api.VariableKey
import spock.lang.Specification

/**
 * Created by timbo on 06/01/16.
 */
class StepsCellJobSpec extends Specification {

    void "basic test"() {

        String producer = "p"
        StepDefinition steps = new StepDefinition(
                EchoStep.class.getName(),
                [:],
                ["input":new VariableKey(producer, "input")],
                ["output":"output"])

        StepsCellExecutorJobDefinition jobdef = new ExecuteCellUsingStepsJobDefinition(1, "cell1", steps);
        MemoryJobStatusClient jobstatusClient = new MemoryJobStatusClient()

        int camelCounter = 0

        when:
        StepsCellJob job = new StepsCellJob(jobdef, jobstatusClient) {
            protected void startJob(CamelContext camelContext, String jobid, String username) {
                camelCounter++
            }
        }
        JobStatus status = job.start(null, "username")


        then:
        status.status == JobStatus.Status.RUNNING
        status.getJobId() != null
        jobstatusClient.list(null).size() == 1
        camelCounter == 1

    }


}
