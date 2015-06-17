package com.im.lac.jobs.impl;

import com.im.lac.jobs.JobStatus;
import com.im.lac.model.DataItem;
import com.im.lac.model.ProcessDatasetJobDefinition;
import com.im.lac.service.DatasetService;
import com.im.lac.service.Environment;
import com.im.lac.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.util.IOHelper;

/**
 *
 * @author timbo
 */
public class JobExecutor {

    private static final Logger LOG = Logger.getLogger(JobExecutor.class.getName());

    public static DataItem submitAndWait(Environment env, ProcessDatasetJobDefinition jobdef) throws Exception {
        //        LOG.log(Level.FINE, "Sending dataset id {0} to endpoint {1}", new Object[]{inputDatasetId, endpoint});
//        final DatasetService service = env.getDatasetService();
//        DataItem dataitem1 = service.doInTransactionWithResult(DataItem.class, (sql) -> {
//            return service.getDataItem(sql, (Long) inputDatasetId);
//        });
//        this.totalCount = 1;
//        String result = env.getExecutorService().getProducerTemplate().requestBody(endpoint, dataitem1, String.class);
//        switch (mode) {
//            case UPDATE:
//                service.doInTransactionWithResult(DataItem.class, (sql) -> {
//                    return service.updateDataItem(sql, dataitem1, new ByteArrayInputStream(result.getBytes()));
//                });
//                this.outputDatasetId = inputDatasetId;
//                break;
//            case CREATE:
//                //Object newId = env.getDatasetService().put(result);
//                DataItem dataitem2 = service.doInTransactionWithResult(DataItem.class, (sql) -> {
//                    return service.addDataItem(sql, dataitem1, new ByteArrayInputStream(result.getBytes()));
//                });
//                this.outputDatasetId = dataitem2.getId();
//                break;
//            default:
//                throw new IllegalStateException("Unexpected mode " + mode);
//        }
        return new DataItem();
    }

    public static <T> void submitProcessDatasetJob(final Environment env, final AsynchronousJob job, final ProcessDatasetJobDefinition jobdef) throws Exception {

        final String endpoint = jobdef.getDestination();
        final Long datasetId = jobdef.getDatasetId();
        final DatasetService service = env.getDatasetService();

        final DataItem[] dataItem = new DataItem[1];
        final InputStream[] is = new InputStream[1];
        LOG.log(Level.INFO, "Getting dataset {0}", datasetId);
        String body = service.doInTransactionWithResult(String.class, (sql) -> {
            try {
                dataItem[0] = service.getDataItem(sql, datasetId);
                LOG.log(Level.INFO, "Getting large object {0}", dataItem[0].getLoid());
                is[0] = service.createLargeObjectReader(sql, dataItem[0].getLoid());
                String res = inputStreamToResult(is[0]);
                return res;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read dataset " + datasetId, ex);
            } finally {
                IOHelper.close(is);
            }
        });

        job.totalCount = 1;
        Future<Exchange> results = env.getExecutorService().getProducerTemplate().asyncCallback(
                endpoint,
                (Exchange exchange) -> {
                    exchange.getIn().setHeader("JobId", job.getJobId());
                    exchange.getIn().setBody(body);
                },
                new Synchronization() {

                    @Override
                    public void onComplete(Exchange exchng) {
                        LOG.info("Job request completed");
                        job.status = JobStatus.Status.RESULTS_READY;
                        T body = (T) exchng.getIn().getBody();
                        handleResults(env, job, jobdef, dataItem[0], body);
                    }

                    @Override
                    public void onFailure(Exchange exchng) {
                        LOG.info("Job request failed");
                        job.status = JobStatus.Status.FAILED;
                    }
                });
    }

    static protected <T> void handleResults(
            final Environment env,
            final AsynchronousJob job,
            final ProcessDatasetJobDefinition jobdef,
            final DataItem dataItem,
            final T result) {
        job.pendingCount = 1;
        final DatasetService service = env.getDatasetService();
        final InputStream is = resultToInputStream(result);
        DataItem di;
        switch (jobdef.getMode()) {
            case UPDATE:
                di = service.doInTransactionWithResult(DataItem.class, (sql) -> {  
                        return service.updateDataItem(dataItem, is);
                });
                break;
            case CREATE:
                DataItem neu = new DataItem();
                neu.setName(jobdef.getDatasetName() == null ? "undefined" : jobdef.getDatasetName());
                neu.setSize(1);
                di = service.doInTransactionWithResult(DataItem.class, (sql) -> {
                    return service.addDataItem(sql, neu, is);
                });
                break;
            default:
                throw new IllegalStateException("Unexpected mode " + jobdef.getMode());
        }
        job.result = di;
        job.pendingCount = 0;
        job.processedCount = 1;
        job.completed = new Date();
        job.status = JobStatus.Status.COMPLETED;
    }

    private static InputStream resultToInputStream(Object obj) {
        return new ByteArrayInputStream(obj.toString().getBytes());
    }

    private static String inputStreamToResult(InputStream is) throws IOException {
        return IOUtils.convertStreamToString(is, 100);
    }

}
