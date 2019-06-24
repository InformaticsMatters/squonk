/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.services.cell;

import org.squonk.core.client.JobStatusRestClient;
import org.squonk.execution.steps.Step;
import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.StepsCellExecutorJobDefinition;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.squonk.core.client.NotebookRestClient;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepExecutor;
import org.squonk.execution.variable.VariableManager;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.util.ServiceConstants;
import org.squonk.util.StatsRecorder;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.*;


/** Consumes a steps job from the message queue and executes it, updating the status of the job accordingly.
 * Created by timbo on 07/01/16.
 */
public class CellExecutorRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(CellExecutorRouteBuilder.class.getName());
    private static final String ROUTE_STATS = "seda:post_stats";

    @Inject
    private JobStatusRestClient jobstatusClient;

    @Inject
    private NotebookRestClient notebookRestClient;

    @Inject
    private MessageQueueCredentials rabbitmqCredentials;

    @Override
    public void configure() throws Exception {

        String jobsMqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_JOB_STEPS_EXCHANGE_NAME, MQUEUE_JOB_STEPS_EXCHANGE_PARAMS) +
                "&concurrentConsumers=10";

        String metricsMqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
                "&routingKey=tokens.squonk";

        // send usage metrics to the message queue
        from(ROUTE_STATS)
                .marshal().json(JsonLibrary.Jackson)
                .to(metricsMqueueUrl);

        LOG.info("Starting to consume from " + jobsMqueueUrl);
        from(jobsMqueueUrl)
                .log("consumed message ${body}")
                .unmarshal().json(JsonLibrary.Jackson, StepsCellExecutorJobDefinition.class)
                .log("JOBDEF: ${body}")
                .process((Exchange exch) -> {
                    processJob(exch);
                });

        from("direct:ping")
                .log("Ping")
                .setBody(constant("Ping"));

//        from("timer:heartbeat?period=10000")
//                .log("Heartbeat " + config)
//                .setBody(constant("Hello world!"))
//                .to("direct:inbound");
    }

    void processJob(Exchange exch) throws IOException {
        dumpHeaders(exch);
        StepsCellExecutorJobDefinition jobdef = exch.getIn().getBody(StepsCellExecutorJobDefinition.class);
        String jobid = exch.getIn().getHeader(StatsRecorder.HEADER_SQUONK_JOB_ID, String.class);
        String username = exch.getIn().getHeader("SquonkUsername", String.class);
        executeJob(exch.getContext(), jobdef, jobid, username);
    }

    void dumpHeaders(Exchange exch) {
        StringBuilder b = new StringBuilder("Headers:");
        for (Map.Entry<String,Object> e:  exch.getIn().getHeaders().entrySet()) {
            b.append("\n  " + e.getKey() + " -> " + e.getValue());
        }
        LOG.info(b.toString());
    }

    void executeJob(CamelContext camelContext, StepsCellExecutorJobDefinition jobdef, String jobid, String username) throws IOException {

        LOG.info("Executing Job: id:" + jobid + " username:" + username);

        Long notebookId = jobdef.getNotebookId();
        Long editableId = jobdef.getEditableId();
        Long cellId = jobdef.getCellId();
        StepDefinition[] steps = jobdef.getSteps();

        if (jobdef == null) {
            throw new IllegalStateException("No Job definition found");
        }
        if (jobid == null) {
            throw new IllegalStateException("No job ID found. Should be present as header named " + ServiceConstants.HEADER_JOB_ID);
        }
        if (username == null) {
            throw new IllegalStateException("No username found. Should be present as header named " + ServiceConstants.HEADER_SQUONK_USERNAME);
        }
        if (notebookId == null) {
            throw new IllegalStateException("No notebookId found. Should be defined in the job definition");
        }
        if (editableId == null) {
            throw new IllegalStateException("No editableId found. Should be defined in the job definition");
        }
        if (cellId == null) {
            throw new IllegalStateException("No cellId found. Should be defined in the job definition");
        }
        if (steps == null) {
            throw new IllegalStateException("No step definitions found. Should be defined in the job definition");
        }

        int i = 1;
        for (StepDefinition stepdef: steps) {
            LOG.info("Step " + i++ + ": " + stepdef.getImplementationClass() +
                    " Service ID: " + stepdef.getServiceId() +
                    " ServiceDescriptor: " + stepdef.getServiceDescriptor());
        }

        VariableManager varman = new VariableManager(notebookRestClient,notebookId, editableId);
        StepExecutor executor = new StepExecutor(cellId, jobid, jobdef, varman, ROUTE_STATS);

        // and execute
        try {
            LOG.fine("Executing job " + jobid + " for user " + username);
            executor.execute(camelContext);
            List<Step> executedSteps = executor.getExecutedSteps();
            String statusMessage = null;
            int numProcessed = 0;
            int numResults = 0;
            int numErrors = 0;
            if (executedSteps != null && !executedSteps.isEmpty()) {
                Step s = executedSteps.get(executedSteps.size() - 1);
                if (s != null) {
                    statusMessage = s.getStatusMessage();
                    if (s.getNumProcessed() > 0) {
                        numProcessed = s.getNumProcessed();
                    }
                    if (s.getNumResults() > 0) {
                        numResults = s.getNumResults();
                    }
                    if (s.getNumErrors() > 0) {
                        numErrors = s.getNumErrors();
                    }
                }
            } else {
                statusMessage = executor.getCurrentStatus();
            }
            LOG.fine("Job " + jobid + " complete. Updating status");

            // TODO - review how stats are set
            jobstatusClient.updateStatus(jobid, JobStatus.Status.COMPLETED, statusMessage, numProcessed, numErrors);
            LOG.info("Status updated to COMPLETED");

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to execute job " + jobid, e);
            jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Failed to execute job. " + e.getMessage());
        } finally {
            executor.cleanup();
        }

    }

}
