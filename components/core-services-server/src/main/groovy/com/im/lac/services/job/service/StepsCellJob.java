package com.im.lac.services.job.service;

import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import com.im.lac.services.dataset.service.DatasetHandler;
import com.im.lac.services.discovery.service.ServiceDescriptorStore;
import com.im.lac.services.job.service.AbstractJob;
import org.apache.camel.CamelContext;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepExecutor;
import org.squonk.execution.variable.VariableLoader;
import org.squonk.execution.variable.VariableManager;
import org.squonk.notebook.api.CellDTO;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by timbo on 31/12/15.
 */
public class StepsCellJob extends AbstractJob<StepsCellExecutorJobDefinition> {

    private static final Logger LOG = Logger.getLogger(AbstractDatasetJob.class.getName());

    private final StepsCellExecutorJobDefinition jobdef;
    private JobStatus<StepsCellExecutorJobDefinition> jobStatus;
    private Date started;
    private Date completed;
    private Exception exception;

    public StepsCellJob(StepsCellExecutorJobDefinition jobdef) {
        this.jobdef = jobdef;
    }

    public StepsCellJob(JobStatus<StepsCellExecutorJobDefinition> jobStatus) {
        this.jobdef = jobStatus.getJobDefinition();
        this.jobStatus = jobStatus;
    }

    @Override
    public JobStatus getCurrentJobStatus() {
        return null;
    }

    @Override
    public JobStatus getUpdatedJobStatus() {
        return null;
    }

    @Override
    public StepsCellExecutorJobDefinition getJobDefinition() {
        return null;
    }

    @Override
    public JobStatus start(CamelContext context, String username) throws Exception {
        LOG.info("start()");
        this.started = new Date();
        JobStore jobStore = JobHandler.getJobStore(context);
        // add to jobStore
        jobStore.putJob(this);

        if (jobdef.getNotebookId() == null) {
            this.status = JobStatus.Status.ERROR;
            this.exception = new NullPointerException("Notebook ID not defined");
            this.completed = new Date();
            return getCurrentJobStatus();
        }
        if (jobdef.getCellName() == null) {
            this.status = JobStatus.Status.ERROR;
            this.exception = new NullPointerException("Cell name not defined");
            this.completed = new Date();
            return getCurrentJobStatus();
        }
        if (jobdef.getSteps() == null || jobdef.getSteps().length == 0) {
            this.status = JobStatus.Status.ERROR;
            this.exception = new NullPointerException("No execution steps defined");
            this.completed = new Date();
            return getCurrentJobStatus();
        }

        // TODO - submit to thread pool
        Thread t = new Thread() {
            @Override
            public void run() {
                executeJob(context);
            }
        };

        this.status = JobStatus.Status.RUNNING;

        return getCurrentJobStatus();
    }

    protected void executeJob(CamelContext context) {
        Long notebookId = jobdef.getNotebookId();
        String cellName = jobdef.getCellName();
        StepDefinition[] steps = jobdef.getSteps();

//        CallbackClientConfig config = ...
//        CallbackContext context = ...
//        CallbackClient client = new CallbackClient(config, context);
//        callbackContext.setNotebookId(notebookId);
//        CellDTO cell = callbackClient.retrieveCell(cellName);
//        if (cell == null) {
//            throw new IllegalStateException("Executor for cell " + cellName + " not found");
//        }

        VariableLoader loader = null; //new CellCallbackClientVariableLoader(client);
        VariableManager varman = new VariableManager(loader);
        StepExecutor executor = new StepExecutor(cellName, varman);

        try {
            executor.execute(steps, context);
            this.status = JobStatus.Status.COMPLETED;
        } catch (Exception e) {
            this.status = JobStatus.Status.ERROR;
            this.exception = e;
        }
        this.completed = new Date();
    }
}
