package org.squonk.services.cell;

import com.im.lac.job.client.JobStatusRestClient;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.notebook.client.CallbackClientConfig;
import org.squonk.notebook.client.DefaultCallbackClientConfig;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.im.lac.services.CommonConstants.HEADER_JOB_ID;
import static com.im.lac.services.CommonConstants.HEADER_SQUONK_USERNAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_STEPS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_STEPS_EXCHANGE_PARAMS;

/** Consunmes a steps job from the message queue and executes it, updating the status of the job accordingly.
 * Created by timbo on 07/01/16.
 */
public class CellExecutorRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(CellExecutorRouteBuilder.class.getName());


    private CallbackClientConfig config = new DefaultCallbackClientConfig();

    private JobStatusRestClient jobstatusClient = new JobStatusRestClient();


    private final MessageQueueCredentials rabbitmqCredentials;

    public CellExecutorRouteBuilder() {
        this.rabbitmqCredentials = new MessageQueueCredentials();
    }

    public CellExecutorRouteBuilder(MessageQueueCredentials rabbitmqCredentials) {
        this.rabbitmqCredentials = rabbitmqCredentials;
    }


    @Override
    public void configure() throws Exception {

        String mqueue = rabbitmqCredentials.generateUrl(MQUEUE_JOB_STEPS_EXCHANGE_NAME, MQUEUE_JOB_STEPS_EXCHANGE_PARAMS);

        //LOG.info("Starting to consume from " + mqueue);
        from(mqueue)
                .log("consumed message ${body}")
                .unmarshal().json(JsonLibrary.Jackson, StepsCellExecutorJobDefinition.class)
                .log("JOBDEF: ${body}")
                .process((Exchange exch) -> {
                    processJob(exch);
                });
    }

    void processJob(Exchange exch) throws IOException {

        CamelContext camelContext = exch.getContext();
        StepsCellExecutorJobDefinition jobdef = exch.getIn().getBody(StepsCellExecutorJobDefinition.class);
        String jobid = exch.getIn().getHeader("rabbitmq.MESSAGE_ID", String.class);
        if (jobid == null) {
            throw new IllegalStateException("No Job ID found. Should be present as header named " + HEADER_JOB_ID);
        }
        String username = exch.getIn().getHeader("rabbitmq.USERID", String.class);
        if (username == null) {
            throw new IllegalStateException("No username found. Should be present as header named " + HEADER_SQUONK_USERNAME);
        }


//        Long notebookId = jobdef.getNotebookId();
//        String cellName = jobdef.getCellName();
        StepDefinition[] steps = jobdef.getSteps();
//
//        // TODO - sort out which client to use. Probably remove the Jersey one and replace the HttpComponents one.
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

        // and execute
        try {
            LOG.info("Executing job " + jobid + " for user " + username);
//            executor.execute(steps, camelContext);
            LOG.info("Job " + jobid + " complete. Updating status");
            jobstatusClient.updateStatus(jobid, JobStatus.Status.COMPLETED);
            LOG.info("Status updated to COMPLETED");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to execute job" + jobid, e);
                jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Failed to execute job. " + e.getMessage());
        }
    }

}
