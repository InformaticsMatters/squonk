package com.im.lac.services.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.im.lac.job.client.JobStatusRestClient;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import com.im.lac.services.job.Job;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.squonk.client.JobStatusClient;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.types.io.JsonHandler;

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
public class StepsCellJob  implements Job<StepsCellExecutorJobDefinition> {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetJob.class.getName());

    protected final StepsCellExecutorJobDefinition jobdef;
    protected String jobid;
    protected static JobStatusClient jobstatusClient = new JobStatusRestClient();
    private static MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials();
    private static final String mqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_JOB_STEPS_EXCHANGE_NAME,MQUEUE_JOB_STEPS_EXCHANGE_PARAMS);

    protected StepsCellJob(StepsCellExecutorJobDefinition jobdef,JobStatusClient jobstatusClient) {
        this.jobdef = jobdef;
        this.jobstatusClient = jobstatusClient;
    }

    public StepsCellJob(StepsCellExecutorJobDefinition jobdef) {
        this.jobdef = jobdef;
        this.jobstatusClient = new JobStatusRestClient();
    }

    public StepsCellJob(JobStatus<StepsCellExecutorJobDefinition> jobStatus) {
        this.jobdef = jobStatus.getJobDefinition();
        this.jobid = jobStatus.getJobId();
        this.jobstatusClient = new JobStatusRestClient();
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

    public JobStatus start(CamelContext camelContext, String username) throws Exception {
        LOG.finer("start() " + jobdef);

        if (jobdef.getNotebookId() == null) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to start job as notebook ID is not defined");
        }
        if (jobdef.getCellName() == null) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to start job as notebook cell name is not defined");
        }
        if (jobdef.getSteps() == null || jobdef.getSteps().length == 0) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to start job as no steps were defined");
        }

        try {
            JobStatus status = jobstatusClient.create(jobdef, username, -1);
            jobid = status.getJobId();
            startJob(camelContext, jobid, username);
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.RUNNING);
        } catch (Throwable t) {
            StringBuilder b = new StringBuilder("Job submission failed: ").append(t.getMessage());
            // TODO - should we pass back the exception as well?
            LOG.log(Level.WARNING, "Job submission failed", t);
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, b.toString());
        }
    }

    protected void startJob(CamelContext camelContext, String jobid, String username) throws JsonProcessingException {
        ProducerTemplate pt = camelContext.createProducerTemplate();
        String json = JsonHandler.getInstance().objectToJson(jobdef);
        Map<String,Object> headers = new HashMap<>();
        headers.put("rabbitmq.ROUTING_KEY", "jobs.steps");
        headers.put("rabbitmq.MESSAGE_ID", jobid);
        headers.put("rabbitmq.USERID", username);
        // send to mqueue
        pt.sendBodyAndHeaders(mqueueUrl, json, headers);
    }

}
