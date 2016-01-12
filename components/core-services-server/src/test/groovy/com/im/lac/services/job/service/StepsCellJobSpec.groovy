package com.im.lac.services.job.service

import com.im.lac.job.jobdef.ExecuteCellUsingStepsJobDefinition
import com.im.lac.job.jobdef.JobDefinition
import com.im.lac.job.jobdef.JobStatus
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition
import com.im.lac.services.job.dao.MemoryJobStatusClient
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.client.JobStatusClient
import org.squonk.execution.steps.StepDefinition
import org.squonk.execution.steps.impl.EchoStep
import org.squonk.notebook.api.VariableKey
import org.squonk.types.io.JsonHandler
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
        String body = null

        CamelContext context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {
                from(SplitAndQueueJobRouteBuilder.ROUTE_STEPS_JOB_SUBMIT)
                .log("Job submitted")
                .process() { Exchange exch ->
                    camelCounter++
                    body = exch.in.getBody(String.class)
                }
            }
        })
        context.start()

        when:
        StepsCellJob job = new StepsCellJob(jobdef, jobstatusClient)
        JobStatus status = job.start(context, "username")


        then:
        status.status == JobStatus.Status.RUNNING
        status.getJobId() != null
        jobstatusClient.list(null).size() == 1
        camelCounter == 1
        body != null
        println "BODY: " + body
        JobDefinition submittedJobDef = JsonHandler.getInstance().objectFromJson(body, JobDefinition.class)
        submittedJobDef instanceof ExecuteCellUsingStepsJobDefinition
        ExecuteCellUsingStepsJobDefinition executeCellUsingStepsJobDefinition = (ExecuteCellUsingStepsJobDefinition)submittedJobDef
        executeCellUsingStepsJobDefinition.getSteps().length == 1
        executeCellUsingStepsJobDefinition.getNotebookId() == 1
        executeCellUsingStepsJobDefinition.getCellName() == "cell1"

        cleanup:
        context.stop()
    }


}
