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

package org.squonk.core.service.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.squonk.client.JobStatusClient;

import org.squonk.core.service.discovery.ServiceDescriptorRegistry;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.core.ServiceDescriptor;
import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.StepsCellExecutorJobDefinition;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.ServiceConstants;
import org.squonk.util.StatsRecorder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_STEPS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_STEPS_EXCHANGE_PARAMS;

/**
 * Created by timbo on 31/12/15.
 */
public class StepsCellJob implements Job<StepsCellExecutorJobDefinition> {

    private static final Logger LOG = Logger.getLogger(StepsCellJob.class.getName());

    protected final StepsCellExecutorJobDefinition jobdef;
    protected String jobid;
    protected final JobStatusClient jobstatusClient;
    private static MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials();
    private static final String mqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_JOB_STEPS_EXCHANGE_NAME, MQUEUE_JOB_STEPS_EXCHANGE_PARAMS);


    public StepsCellJob(JobStatusClient jobstatusClient, StepsCellExecutorJobDefinition jobdef) {
        this.jobstatusClient = jobstatusClient;
        this.jobdef = jobdef;
    }

    public StepsCellJob(JobStatusClient jobstatusClient, JobStatus<StepsCellExecutorJobDefinition> jobStatus) {
        this.jobstatusClient = jobstatusClient;
        this.jobdef = jobStatus.getJobDefinition();
        this.jobid = jobStatus.getJobId();
    }

    @Override
    public void setStatus(JobStatus.Status status) throws IOException {
        jobstatusClient.updateStatus(jobid, status, null);
    }

    @Override
    public JobStatus.Status getStatus() throws IOException {
        return jobstatusClient.get(jobid).getStatus();
    }

    @Override
    public String getJobId() {
        return jobid;
    }

    @Override
    public JobStatus getCurrentJobStatus() throws IOException {
        return jobstatusClient.get(jobid);
    }

    @Override
    public JobStatus getUpdatedJobStatus() throws IOException {
        return getCurrentJobStatus();
    }

    @Override
    public StepsCellExecutorJobDefinition getJobDefinition() {
        return jobdef;
    }

    public JobStatus start(CamelContext camelContext, String username, Integer totalCount) throws Exception {
        LOG.info("submit() " + jobdef);

        if (jobdef.getNotebookId() == null) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to submit job as notebook ID is not defined");
        }
        if (jobdef.getEditableId() == null) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to submit job as editable ID is not defined");
        }
        if (jobdef.getCellId() == null) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to submit job as notebook cell ID is not defined");
        }
        if (jobdef.getSteps() == null || jobdef.getSteps().length == 0) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to submit job as no steps were defined");
        }

        try {
            JobStatus status = jobstatusClient.submit(jobdef, username, totalCount);
            jobid = status.getJobId();
            for (StepDefinition stepdef : jobdef.getSteps()) {
                String serviceId = stepdef.getServiceId();
                if (serviceId != null) {
                    LOG.info("Looking up ServiceDescriptor for " + serviceId);
                    ServiceDescriptorRegistry reg = camelContext.getRegistry().lookupByNameAndType(ServiceConstants.KEY_SERVICE_REGISTRY, ServiceDescriptorRegistry.class);
                    ServiceDescriptor sd = reg.fetchServiceDescriptor(serviceId);
                    stepdef.setServiceDescriptor(sd);
                }
            }
            startJob(camelContext, username);
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.RUNNING);
        } catch (Throwable t) {
            StringBuilder b = new StringBuilder("Job submission failed: ").append(t.getMessage());
            // TODO - should we pass back the exception as well?
            LOG.log(Level.WARNING, "Job submission failed", t);
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, b.toString());
        }
    }

    protected void startJob(CamelContext camelContext, String username) throws JsonProcessingException {
        LOG.info("Starting Job for user " + username);
        ProducerTemplate pt = camelContext.createProducerTemplate();
        String json = JsonHandler.getInstance().objectToJson(jobdef);
        LOG.info("JSON: " + json);
        Map<String, Object> headers = new HashMap<>();
        headers.put("rabbitmq.ROUTING_KEY", "jobs.steps");
        headers.put(StatsRecorder.HEADER_SQUONK_JOB_ID, jobid);
        headers.put(ServiceConstants.HEADER_SQUONK_USERNAME, username);
        // send to mqueue
        LOG.info("Sending job to queue " + mqueueUrl + " ->\n" + json);
        pt.sendBodyAndHeaders(mqueueUrl, json, headers);
    }

}
