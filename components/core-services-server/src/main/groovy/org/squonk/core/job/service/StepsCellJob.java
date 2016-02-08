package org.squonk.core.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import org.squonk.core.job.Job;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
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

    private static final Logger LOG = Logger.getLogger(StepsCellJob.class.getName());

    protected final StepsCellExecutorJobDefinition jobdef;
    protected String jobid;
    protected static MemoryJobStatusClient jobstatusClient = MemoryJobStatusClient.INSTANCE;
    private static MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials();
    private static final String mqueueUrl = rabbitmqCredentials.generateUrl(MQUEUE_JOB_STEPS_EXCHANGE_NAME,MQUEUE_JOB_STEPS_EXCHANGE_PARAMS);


    public StepsCellJob(StepsCellExecutorJobDefinition jobdef) {
        this.jobdef = jobdef;
    }

    public StepsCellJob(JobStatus<StepsCellExecutorJobDefinition> jobStatus) {
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
        if (jobdef.getCellName() == null) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to submit job as notebook cell name is not defined");
        }
        if (jobdef.getSteps() == null || jobdef.getSteps().length == 0) {
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Unable to submit job as no steps were defined");
        }

        try {
            JobStatus status = jobstatusClient.submit(jobdef, username, totalCount);
            jobid = status.getJobId();
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
        Map<String,Object> headers = new HashMap<>();
        headers.put("rabbitmq.ROUTING_KEY", "jobs.steps");
        headers.put("SquonkJobID", jobid);
        headers.put("SquonkUsername", username);
        // send to mqueue
        LOG.info("Sending job to queue " + mqueueUrl + " ->\n" + json);
        pt.sendBodyAndHeaders(mqueueUrl, json, headers);
    }

}
