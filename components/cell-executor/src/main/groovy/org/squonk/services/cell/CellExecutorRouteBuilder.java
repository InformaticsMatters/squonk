package org.squonk.services.cell;

import com.im.lac.job.client.JobStatusRestClient;
import com.im.lac.job.jobdef.JobStatus;
import com.im.lac.job.jobdef.StepsCellExecutorJobDefinition;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.squonk.execution.steps.StepDefinition;
import org.squonk.execution.steps.StepExecutor;
import org.squonk.execution.variable.VariableLoader;
import org.squonk.execution.variable.VariableManager;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.client.CallbackClient;
import org.squonk.notebook.client.CallbackClientConfig;
import org.squonk.notebook.client.CallbackContext;

import javax.inject.Inject;
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

    @Inject
    private CallbackClientConfig config;

    @Inject
    private JobStatusRestClient jobstatusClient;

    @Inject
    private MessageQueueCredentials rabbitmqCredentials;

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

        from("direct:ping")
                .log("Ping")
                .setBody(constant("Ping"));

//        from("timer:heartbeat?period=10000")
//                .log("Heartbeat " + config)
//                .setBody(constant("Hello world!"))
//                .to("direct:inbound");
    }

    void processJob(Exchange exch) throws IOException {
        StepsCellExecutorJobDefinition jobdef = exch.getIn().getBody(StepsCellExecutorJobDefinition.class);
        String jobid = exch.getIn().getHeader("rabbitmq.MESSAGE_ID", String.class);
        String username = exch.getIn().getHeader("rabbitmq.USERID", String.class);
        executeJob(exch.getContext(), jobdef, jobid, username);
    }

    void executeJob(CamelContext camelContext, StepsCellExecutorJobDefinition jobdef, String jobid, String username) throws IOException {

        Long notebookId = jobdef.getNotebookId();
        String cellName = jobdef.getCellName();
        StepDefinition[] steps = jobdef.getSteps();

        if (jobdef == null) {
            throw new IllegalStateException("No Job definition found");
        }
        if (jobid == null) {
            throw new IllegalStateException("No job ID found. Should be present as header named " + HEADER_JOB_ID);
        }
        if (username == null) {
            throw new IllegalStateException("No username found. Should be present as header named " + HEADER_SQUONK_USERNAME);
        }
        if (notebookId == null) {
            throw new IllegalStateException("No notebookId found. Should be defined in the job definition");
        }
        if (cellName == null) {
            throw new IllegalStateException("No cellName found. Should be defined in the job definition");
        }
        if (steps == null) {
            throw new IllegalStateException("No step definitions found. Should be defined in the job definition");
        }


        // TODO - sort out which client to use. Probably remove the Jersey one and replace the HttpComponents one?
        // @Inject these?
        CallbackContext callbackContext = new CallbackContext();
        CallbackClient callbackClient = new CallbackClient(config, callbackContext);
        callbackContext.setNotebookId(notebookId);

        // fetch the cell
        CellDTO cell = callbackClient.retrieveCell(cellName);
        if (cell == null) {
            throw new IllegalStateException("Executor for cell " + cellName + " not found");
        }

        // setup the variable manager - @Inject these?
        VariableLoader loader = new CellCallbackClientVariableLoader(callbackClient);
        VariableManager varman = new VariableManager(loader);
        StepExecutor executor = new StepExecutor(cellName, varman);

        // and execute
        try {
            LOG.info("Executing job " + jobid + " for user " + username);
            executor.execute(steps, camelContext);
            LOG.info("Job " + jobid + " complete. Updating status");
            jobstatusClient.updateStatus(jobid, JobStatus.Status.COMPLETED);
            LOG.info("Status updated to COMPLETED");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to execute job" + jobid, e);
                jobstatusClient.updateStatus(jobid, JobStatus.Status.ERROR, "Failed to execute job. " + e.getMessage());
        }
    }

}
