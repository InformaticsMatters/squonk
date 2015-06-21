package com.im.lac.jobs.impl;

import com.im.lac.jobs.JobStatus;
import com.im.lac.model.DataItem;
import com.im.lac.model.DatasetJobDefinition;
import com.im.lac.service.DatasetService;
import com.im.lac.service.JobStore;
import com.im.lac.types.io.JsonHandler;
import com.im.lac.types.io.Metadata;
import com.im.lac.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class DatasetHandler {

    private final FileCache cache;
    private final JsonHandler jsonHandler = new JsonHandler();
    private final DatasetService service;

    public DatasetHandler(DatasetService service, String cachePath) throws IOException {
        this.service = service;
        cache = new FileCache(cachePath);
        cache.init();
    }

    public void deleteDataset(Long datasetId) throws Exception {
        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            DataItem di = service.getDataItem(sql, datasetId);
            if (di != null) {
                service.deleteDataItem(sql, di);
            }
            return di;
        });
        deleteFileFromCache(dataItem);
    }

    public Object fetchDatasetObjectsForJob(final AbstractDatasetJob job) throws Exception {
        DatasetJobDefinition jobdef = job.getJobDefinition();
        return fetchDatasetObjectsForId(jobdef.getDatasetId());
    }

    public Object fetchDatasetObjectsForId(Long datasetId) throws Exception {
        return generateObjectFromJson(fetchDatasetJsonForId(datasetId));
    }

    public JsonProcessingHolder fetchDatasetJsonForId(Long datasetId) throws Exception {
        JsonProcessingHolder holder = service.doInTransactionWithResult(JsonProcessingHolder.class, (sql) -> {
            DataItem di = service.getDataItem(sql, datasetId);
            File f = cache.getFileFromCache(di.getLoid());
            if (f == null) {
                try (InputStream in = service.createLargeObjectReader(sql, di.getLoid())) {
                    f = cache.addFileToCache(di.getLoid(), in);
                } catch (IOException ioe) {
                    throw new RuntimeException("Failed fetching dataset", ioe);
                }
            }
            try {
                return new JsonProcessingHolder(IOUtils.getGunzippedInputStream(new FileInputStream(f)), di.getMetadata());
            } catch (IOException ex) {
                throw new RuntimeException("Failed fetching cached file for dataset", ex);
            }
        });
        return holder;
    }

    public JobStatus saveDataset(Object results, Exchange exchange) throws Exception {
        String jobId = exchange.getIn().getHeader("JobId", String.class);
        AbstractDatasetJob job = (AbstractDatasetJob) exchange.getContext().getRegistry().lookupByNameAndType(CamelExecutor.JOB_STORE, JobStore.class).getJob(jobId);
        job.status = JobStatus.Status.RESULTS_READY;
        JsonProcessingHolder marshalResults = generateJsonForItem(results, true);

        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            try {
                DataItem orig = service.getDataItem(sql, job.getJobDefinition().getDatasetId());
                orig.setMetadata(marshalResults.metadata);
                deleteFileFromCache(orig);
                DataItem neu;
                switch (job.getJobDefinition().getMode()) {
                    case UPDATE:
                        neu = service.updateDataItem(sql, orig, marshalResults.inputStream);
                        break;
                    case CREATE:
                        DataItem di = new DataItem();
                        di.setName(job.getJobDefinition().getDatasetName() == null ? "undefined" : job.getJobDefinition().getDatasetName());
                        di.setSize(1);
                        di.setMetadata(marshalResults.metadata);
                        neu = service.addDataItem(sql, di, marshalResults.inputStream);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected mode " + job.getJobDefinition().getMode());
                }
                marshalResults.inputStream.close();
                return neu;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write dataset", ex);
            }
        });

        job.status = JobStatus.Status.COMPLETED;
        job.result = dataItem;
        return job.buildStatus();
    }

    private void deleteFileFromCache(DataItem dataItem) {
        if (dataItem != null && dataItem.getLoid() != null) {
            cache.deleteFileFromCache(dataItem.getLoid());
        }
    }

    /**
     * Reads JSON from the InputStream and generates object(s) according to the metadata definition
     *
     * @param meta
     * @param is
     * @return
     * @throws Exception
     */
    Object generateObjectFromJson(JsonProcessingHolder holder) throws Exception {

        switch (holder.metadata.getType()) {
            case TEXT:
                return IOUtils.convertStreamToString(holder.inputStream, 100);
            case ITEM:
                return jsonHandler.unmarshalItem(holder.metadata, holder.inputStream);
            case ARRAY:
                return jsonHandler.unmarshalItemsAsStream(holder.metadata, holder.inputStream);
            default:
                throw new IllegalStateException("Unimplemented dataset type: " + holder.metadata.getType());
        }
    }

    /**
     * Takes the object(s) and generates JSON and corresponding metadata.
     *
     * @param item The Object, Stream or Iterable to marshal to JSON.
     * @param gzip Whether to gzip the stream. Usually this inputStream best as it reduces IO.
     * @return the marshal results, with the metadata complete once the InputStream has been fully
     * read.
     * @throws IOException
     */
    JsonProcessingHolder generateJsonForItem(Object item, boolean gzip) throws IOException {
        final PipedInputStream pis = new PipedInputStream();
        final OutputStream out = new PipedOutputStream(pis);
        final Metadata meta = new Metadata();

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable c = (Callable) () -> {
            jsonHandler.marshalItem(item, meta, gzip ? new GZIPOutputStream(out) : out);
            executor.shutdown();
            return true;
        };
        executor.submit(c);
        return new JsonProcessingHolder(pis, meta);
    }

    public static class JsonProcessingHolder {

        InputStream inputStream;
        Metadata metadata;

        JsonProcessingHolder(InputStream inputStream, Metadata metadata) {
            this.inputStream = inputStream;
            this.metadata = metadata;
        }
    }

}
