package com.im.lac.jobs.impl;

import com.im.lac.service.impl.SimpleFileCacheService;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.util.IOHelper;

/**
 *
 * @author timbo
 */
public class DatasetHandler {

    private final SimpleFileCacheService cache;
    private final JsonHandler jsonHandler = new JsonHandler();
    private final DatasetService service;

    public DatasetHandler(DatasetService service, String cachePath) throws IOException {
        this.service = service;
        cache = new SimpleFileCacheService(cachePath);
        cache.init();
    }

    public List<DataItem> listDataItems(Object body) throws Exception {
        // body is ignored, but it could be a filter?
        return service.getDataItems();
    }

    public DataItem getDataItem(final Long id) throws Exception {
        return service.getDataItem(id);
    }

    public void deleteDataset(Long datasetId) throws Exception {
        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            DataItem di = service.getDataItem(sql, datasetId);
            if (di != null) {
                try {
                    service.deleteDataItem(sql, di);
                } catch (Exception ioe) {
                    throw new RuntimeException("Failed fetching dataset", ioe);
                }
            }
            return di;
        });
        deleteFileFromCache(dataItem);
    }

    public Object fetchObjectsForJob(final AbstractDatasetJob job) throws Exception {
        DatasetJobDefinition jobdef = job.getJobDefinition();
        return fetchObjectsForDataset(jobdef.getDatasetId());
    }

    public Object fetchObjectsForDataset(Long datasetId) throws Exception {
        return generateObjectFromJson(fetchJsonForDataset(datasetId));
    }
    
    public Object fetchObjectsForId(Long datasetId) throws Exception {
        JsonProcessingHolder holder = fetchJsonForDataset(datasetId);
        return generateObjectFromJson(holder);
    }

    public JsonProcessingHolder fetchJsonForDataset(Long datasetId) throws Exception {
        JsonProcessingHolder holder = service.doInTransactionWithResult(JsonProcessingHolder.class, (sql) -> {
            DataItem di = service.getDataItem(sql, datasetId);
            File f = cache.getFileFromCache(di.getLoid());
            if (f == null) {
                try (InputStream in = service.createLargeObjectReader(sql, di.getLoid())) {
                    f = cache.addFileToCache(di.getLoid(), in);
                } catch (Exception ioe) {
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

    public JobStatus saveDatasetForJob(Object results, Exchange exchange) throws Exception {
        String jobId = exchange.getIn().getHeader("JobId", String.class);
        AbstractDatasetJob job = (AbstractDatasetJob) exchange.getContext().getRegistry().lookupByNameAndType(CamelExecutor.JOB_STORE, JobStore.class).getJob(jobId);
        job.setStatus(JobStatus.Status.RESULTS_READY);

        DataItem dataItem;
        switch (job.getJobDefinition().getMode()) {
            case UPDATE:
                dataItem = updateDataset(results, job.getJobDefinition().getDatasetId());
                break;
            case CREATE:
                String name = job.getJobDefinition().getDatasetName();
                dataItem = createDataset(results, name == null ? "undefined" : name);
                break;
            default:
                throw new IllegalStateException("Unexpected mode " + job.getJobDefinition().getMode());
        }
        job.setStatus(JobStatus.Status.COMPLETED);
        job.result = dataItem;
        return job.buildStatus();
    }

    public DataItem updateDataset(final Object data, final Long datsetId) throws Exception {
        final JsonProcessingHolder marshalResults = generateJsonForItem(data, true);
        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            try {
                DataItem orig = service.getDataItem(sql, datsetId);
                orig.setMetadata(marshalResults.metadata);
                deleteFileFromCache(orig);
                return service.updateDataItem(sql, orig, marshalResults.inputStream);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to write dataset", ex);
            } finally {
                IOHelper.close(marshalResults.inputStream);
            }
        });
        return dataItem;
    }

    public DataItem createDataset(final Object newData, final String datasetName) throws Exception {
        final JsonProcessingHolder marshalResults = generateJsonForItem(newData, true);
        final DataItem neu = new DataItem();
        neu.setName(datasetName);
        neu.setMetadata(marshalResults.metadata);
        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            try {
                return service.addDataItem(sql, neu, marshalResults.inputStream);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create dataset", ex);
            } finally {
                IOHelper.close(marshalResults.inputStream);
            }
        });
        return dataItem;
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
            if (item instanceof Stream) {
                jsonHandler.marshalItems((Stream)item, meta, gzip ? new GZIPOutputStream(out) : out);
            } else {
                jsonHandler.marshalItem(item, meta, gzip ? new GZIPOutputStream(out) : out);
            }
            executor.shutdown();
            return true;
        };
        executor.submit(c);
        return new JsonProcessingHolder(pis, meta);

    }

    public static class JsonProcessingHolder {

        final InputStream inputStream;
        final Metadata metadata;

        JsonProcessingHolder(InputStream inputStream, Metadata metadata) {
            this.inputStream = inputStream;
            this.metadata = metadata;
        }
        
        public InputStream getInputStream() {
            return inputStream;
        }
        
        public Metadata getMetadata() {
            return metadata;
        }
        
    }

}
