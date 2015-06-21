package com.im.lac.jobs.impl;

import com.im.lac.jobs.JobStatus;
import com.im.lac.model.DatasetJobDefinition;
import com.im.lac.service.Environment;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelExecutionException;

/**
 * Asynchronous executor for jobs that typically take a few seconds or minutes to complete and
 * handle a sequence of data.
 * <p>
 * The job remains active during execution and automatically processes the results in the
 * background. The general pattern is the same as for {@link AsynchronousJob} except that the input
 * and output is expected to be a Stream or Iterable of objects of some type.</p>
 *
 * @author timbo
 */
public class AsynchronousStreamingJob<T extends DatasetJobDefinition> extends AbstractDatasetJob<T> {

    private static final Logger LOG = Logger.getLogger(AsynchronousStreamingJob.class.getName());

    private final String queueName;
    // TODO - better way to handle this? For now we give up if no results after 1 hour
    private final int timeoutInSeconds = 60 * 60;

    /**
     *
     * @param jobdef
     * @param queueName The JMS queue name to post the result to.
     */
    public AsynchronousStreamingJob(T jobdef, String queueName) {
        super(jobdef);
        this.queueName = queueName;
    }

    public String getRequestQueueUri() {
        return CamelExecutor.JMS_BROKER_NAME + ":queue:" + queueName;
    }

    @Override
    public JobStatus doExecute(Environment env) throws CamelExecutionException {
        LOG.log(Level.FINE, "QueueJob.execute() submitting job {0} to queue {1}", new Object[]{jobdef, queueName});
//        Object dataset = null; ////env.getDatasetService().get(inputDatasetId);
//        Future<Object> results = env.getExecutorService().getProducerTemplate().asyncRequestBody(getRequestQueueUri(), dataset);
//        this.totalCount = 1; // TODO this value is wrong
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                handleResults(env, results);
//            }
//        };
//        t.start();
        return null;
    }

//    protected JobStatus handleResults(Environment env, Future<Object> result) {
//
//        Object items;
//        try {
//            items = result.get(timeoutInSeconds, TimeUnit.SECONDS);
//        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
//            LOG.log(Level.SEVERE, "Getting results from Future failed ", ex);
//            this.status = JobStatus.Status.ERROR;
//            this.exception = ex;
//            return buildStatus();
//        }
//        this.status = JobStatus.Status.RESULTS_READY;
//
//        this.pendingCount = 1;
//        LOG.log(Level.FINE, "Received results: {0}", items);
//        List<T> results = generateResults(items);
//        this.processedCount = results.size();
//
//        switch (mode) {
//            case UPDATE:
//                ////env.getDatasetService().update(inputDatasetId, results);
//                this.outputDatasetId = inputDatasetId;
//                break;
//            case CREATE:
//                Object newId = null;////env.getDatasetService().put(results);
//                this.outputDatasetId = newId;
//                break;
//            default:
//                throw new IllegalStateException("Unexpected mode " + mode);
//        }
//        this.pendingCount = 0;
//        this.processedCount = 1;
//        this.status = JobStatus.Status.COMPLETED;
//        this.completed = new Date();
//        return buildStatus();
//    }
//
//    private List<T> generateResults(Object results) {
//        // TODO convert to an InputStream containing the necessary JSON
//        if (results instanceof List) {
//            return (List<T>) results;
//        } else if (results instanceof Stream) {
//            return ((Stream<T>) results).collect(Collectors.toList());
//        } else if (results instanceof Iterable) {
//            List<T> list = new ArrayList<>();
//            for (T item : (Iterable<T>) results) {
//                list.add(item);
//            }
//            return list;
//        }
//        throw new IllegalArgumentException("Unexpected type: " + results.getClass().getName());
//    }

}
