package com.im.lac.services.job.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import com.im.lac.services.job.Job;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.squonk.client.JobStatusClient;
import org.squonk.types.io.JsonHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 31/12/15.
 */
public class StepsCellJob
        implements Job<StepsCellExecutorJobDefinition> {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetJob.class.getName());

    private final StepsCellExecutorJobDefinition jobdef;
    private String jobid;
    private final JobStatusClient jobstatusClient;

    public StepsCellJob(StepsCellExecutorJobDefinition jobdef, JobStatusClient jobstatusClient) {
        this.jobdef = jobdef;
        this.jobstatusClient = jobstatusClient;
    }

    public StepsCellJob(JobStatus<StepsCellExecutorJobDefinition> jobStatus, JobStatusClient jobstatusClient) {
        this.jobdef = jobStatus.getJobDefinition();
        this.jobid = jobStatus.getJobId();
        this.jobstatusClient = jobstatusClient;
    }


    @Override
    public void setStatus(JobStatus.Status status) {
        jobstatusClient.updateStatus(jobid, status, null);
    }

    @Override
    public JobStatus.Status getStatus() {
        return jobstatusClient.get(jobid).getStatus();
    }

    @Override
    public String getJobId() {
        return jobid;
    }

    @Override
    public JobStatus getCurrentJobStatus() {
        return jobstatusClient.get(jobid);
    }

    @Override
    public JobStatus getUpdatedJobStatus() {
        return getCurrentJobStatus();
    }

    @Override
    public StepsCellExecutorJobDefinition getJobDefinition() {
        return jobdef;
    }

    public JobStatus start(CamelContext camelContext, String username) throws Exception {
        LOG.info("start()");
        jobid = jobstatusClient.create(jobdef, -1);


        if (jobdef.getNotebookId() == null) {
            // TODO - handle description for error
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, 0);
        }
        if (jobdef.getCellName() == null) {
            // TODO - handle description for error
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, 0);
        }
        if (jobdef.getSteps() == null || jobdef.getSteps().length == 0) {
            // TODO - handle description for error
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, 0);
        }

        try {
            startJob(camelContext);
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.RUNNING, 0);
        } catch (Throwable t) {
            // TODO - handle description for error
            return jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, 0);
        }
    }

    protected void startJob(CamelContext camelContext) throws JsonProcessingException {
        ProducerTemplate pt = camelContext.createProducerTemplate();
        String json = JsonHandler.getInstance().objectToJson(jobdef);
        Map<String,Object> headers = new HashMap<>();
        headers.put("rabbitmq.ROUTING_KEY", "jobs.steps");
        pt.sendBodyAndHeaders(SplitAndQueueJobRouteBuilder.ROUTE_STEPS_JOB_SUBMIT, json, headers);
    }

//    protected void executeJob(CamelContext camelContext) {
//        Long notebookId = jobdef.getNotebookId();
//        String cellName = jobdef.getCellName();
//        StepDefinition[] steps = jobdef.getSteps();
//
//        // TODO - sort out which client to use. Probably remove the Jersey one and replace the HttpComponents one.
//        CallbackClientConfig config = new DefaultCallbackClientConfig();
//        CallbackContext callbackContext = new CallbackContext();
//        CallbackClient callbackClient = new CallbackClient(config, callbackContext);
//        callbackContext.setNotebookId(notebookId);
//
//        // fetch the cell
//        CellDTO cell = callbackClient.retrieveCell(cellName);
//        if (cell == null) {
//            throw new IllegalStateException("Executor for cell " + cellName + " not found");
//        }
//
//        // setup the variable manager
//        VariableLoader loader = new CellCallbackClientVariableLoader(callbackClient);
//        VariableManager varman = new VariableManager(loader);
//        StepExecutor executor = new StepExecutor(cellName, varman);
//
//        // and execute
//        try {
//
//            executor.execute(steps, camelContext);
//            jobstatusClient.updateStatus(jobid, JobStatus.Status.COMPLETED, 0);
//        } catch (Exception e) {
//            // TODO - handle description for error
//            jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, 0);
//        }
//    }

}
