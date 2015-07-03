package com.im.lac.services.dataset.service;

import com.im.lac.dataset.DataItem;
import com.im.lac.types.io.JsonHandler;
import com.im.lac.dataset.Metadata;
import com.im.lac.services.ServerConstants;
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
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.util.IOHelper;

/**
 *
 * @author timbo
 */
public class DatasetHandler {

    private static final Logger LOG = Logger.getLogger(DatasetHandler.class.getName());

    protected final SimpleFileCacheService cache;
    protected final JsonHandler jsonHandler = new JsonHandler();
    protected final DatasetServiceImpl service;

    public DatasetHandler(DatasetServiceImpl service, String cachePath) throws IOException {
        this.service = service;
        cache = new SimpleFileCacheService(cachePath);
        cache.init();
    }

    static DatasetHandler getDatasetHandler(Exchange exchange) {
        return getDatasetHandler(exchange.getContext());
    }

    static DatasetHandler getDatasetHandler(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(ServerConstants.DATASET_HANDLER, DatasetHandler.class);
    }

    public static void putDataItems(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        exchange.getIn().setBody(dh.listDataItems());
    }

    public List<DataItem> listDataItems() throws Exception {
        return service.getDataItems();
    }

    public static void putDataItem(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        Long datasetId = exchange.getIn().getHeader(ServerConstants.REST_DATASET_ID, Long.class);
        if (datasetId == null) {
            throw new IllegalArgumentException("Header " + ServerConstants.REST_DATASET_ID + " not defined. Don't know which dataset to fetch.");
        }
        DataItem dataItem = dh.fetchDataItem(datasetId);
        if (dataItem == null) {
            throw new IllegalArgumentException("Dataset " + datasetId + " not found.");
        }
        exchange.getIn().setBody(dataItem);
    }

    public DataItem fetchDataItem(final Long id) throws Exception {
        return service.getDataItem(id);
    }

    public static void deleteDataset(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        Long datasetId = exchange.getIn().getHeader(ServerConstants.REST_DATASET_ID, Long.class);
        dh.deleteDataset(datasetId);
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

    public Object fetchObjectsForDataset(Long datasetId) throws Exception {
        return generateObjectFromJson(fetchJsonForDataset(datasetId));
    }

    public Object fetchObjectsForId(Long datasetId) throws Exception {
        JsonProcessingHolder holder = fetchJsonForDataset(datasetId);
        return generateObjectFromJson(holder);
    }

    public static void putJsonForDataset(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        Long datasetId = exchange.getIn().getHeader(ServerConstants.REST_DATASET_ID, Long.class);
        JsonProcessingHolder holder = dh.fetchJsonForDataset(datasetId);
        exchange.getIn().setBody(holder);
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
                DataItem di = service.addDataItem(sql, neu, marshalResults.inputStream);
                return di;
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
     * @param holder
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
                jsonHandler.marshalItems((Stream) item, meta, gzip ? new GZIPOutputStream(out) : out);
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
