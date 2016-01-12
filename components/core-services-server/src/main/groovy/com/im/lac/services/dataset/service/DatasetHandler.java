package com.im.lac.services.dataset.service;

import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.dataset.DataItem;
import org.squonk.types.io.JsonHandler;
import com.im.lac.dataset.Metadata;
import com.im.lac.services.ServerConstants;
import com.im.lac.services.util.Utils;
import org.squonk.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.util.IOHelper;

/**
 *
 * @author timbo
 */
public class DatasetHandler implements ServerConstants {

    private static final Logger LOG = Logger.getLogger(DatasetHandler.class.getName());

    protected final SimpleFileCacheService cache;
    protected final JsonHandler jsonHandler = new JsonHandler();
    protected final DatasetService service;

    public DatasetHandler(DatasetService service, String cachePath) throws IOException {
        this.service = service;
        cache = new SimpleFileCacheService(cachePath);
        cache.init();
    }

    /**
     * Deletes all data and sets up some test data. FOR TESTING PURPOSES ONLY.
     *
     * @param username
     * @return
     */
    public List<Long> createTestData(String username) {
        service.deleteDataForUser(username);
        return service.createTestData(username);
    }

    static DatasetHandler getDatasetHandler(Exchange exchange) {
        return getDatasetHandler(exchange.getContext());
    }

    static DatasetHandler getDatasetHandler(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(DATASET_HANDLER, DatasetHandler.class);
    }

    public static void putDataItems(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        exchange.getIn().setBody(dh.listDataItems(Utils.fetchUsername(exchange)));
    }

    public List<DataItem> listDataItems(String username) throws Exception {
        return service.getDataItems(username);
    }

    public static void putDataItem(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        Long datasetId = exchange.getIn().getHeader(REST_DATASET_ID, Long.class);
        if (datasetId == null) {
            throw new IllegalArgumentException("Header " + REST_DATASET_ID + " not defined. Don't know which dataset to fetch.");
        }
        DataItem dataItem = dh.fetchDataItem(Utils.fetchUsername(exchange), datasetId);
        if (dataItem == null) {
            throw new IllegalArgumentException("Dataset " + datasetId + " not found.");
        }
        exchange.getIn().setBody(dataItem);
    }

    public DataItem fetchDataItem(final String username, final Long id) throws Exception {
        return service.getDataItem(username, id);
    }

    public static void deleteDataset(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        Long datasetId = exchange.getIn().getHeader(REST_DATASET_ID, Long.class);
        if (datasetId == null) {
            throw new IllegalStateException("Dataset not specified");
        }

        dh.deleteDataset(Utils.fetchUsername(exchange), datasetId);
    }

    public void deleteDataset(final String username, Long datasetId) throws Exception {
        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            DataItem di = service.getDataItem(sql, username, datasetId);
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

    public Object fetchObjectsForDataset(String username, Long datasetId) throws Exception {
        JsonMetadataPair holder = fetchJsonForDataset(username, datasetId);
        return generateObjectFromJson(holder.getInputStream(), holder.getMetadata());
    }

    public static void putJsonForDataset(Exchange exchange) throws Exception {
        DatasetHandler dh = getDatasetHandler(exchange);
        Long datasetId = exchange.getIn().getHeader(REST_DATASET_ID, Long.class);
        JsonMetadataPair holder = dh.fetchJsonForDataset(Utils.fetchUsername(exchange), datasetId);
        exchange.getIn().setBody(holder);
    }

    public JsonMetadataPair fetchJsonForDataset(String username, Long datasetId) throws Exception {
        JsonMetadataPair holder = service.doInTransactionWithResult(JsonMetadataPair.class, (sql) -> {
            DataItem di = service.getDataItem(sql, username, datasetId);
            LOG.log(Level.INFO, "DataItem for id {0} is {1}", new Object[]{datasetId, di});
            File f = cache.getFileFromCache(di.getLoid());
            if (f == null) {
                try (InputStream in = service.createLargeObjectReader(sql, di.getLoid())) {
                    f = cache.addFileToCache(di.getLoid(), in);
                } catch (Exception ioe) {
                    throw new RuntimeException("Failed fetching dataset", ioe);
                }
            }
            try {
                return new JsonMetadataPair(IOUtils.getGunzippedInputStream(new FileInputStream(f)), di.getMetadata());
            } catch (IOException ex) {
                throw new RuntimeException("Failed fetching cached file for dataset", ex);
            }
        });
        return holder;
    }

    /**
     * Update the dataset with these Object(s)
     *
     * @param username
     * @param data
     * @param datsetId
     * @return
     * @throws Exception
     */
    public DataItem updateDataset(final String username, final Object data, final Long datsetId) throws Exception {
        final JsonMetadataPair marshalResults = jsonHandler.generateJsonForItem(data, true);
        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            try {
                DataItem orig = service.getDataItem(sql, username, datsetId);

                orig.setMetadata(marshalResults.getMetadata());
                deleteFileFromCache(orig);
                return service.updateDataItem(sql, username, orig, marshalResults.getInputStream());
            } catch (Exception ex) {
                throw new RuntimeException("Failed to write dataset", ex);
            } finally {
                IOHelper.close(marshalResults.getInputStream());
            }
        });
        return dataItem;
    }

    public DataItem createDataset(final String username, final Object newData, final String datasetName) throws Exception {
        final JsonMetadataPair marshalResults = jsonHandler.generateJsonForItem(newData, true);

        final DataItem neu = new DataItem();
        neu.setName(datasetName);
        neu.setMetadata(marshalResults.getMetadata());

        DataItem dataItem = service.doInTransactionWithResult(DataItem.class, (sql) -> {
            try {
                DataItem di = service.addDataItem(sql, username, neu, marshalResults.getInputStream());
                return di;
            } catch (Exception ex) {
                throw new RuntimeException("Failed to submit dataset", ex);
            } finally {
                IOHelper.close(marshalResults.getInputStream());
            }
        });
        LOG.info("Created dataset with dataitem:" + dataItem);
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
     * @param inputStream
     * @param metadata
     * @return
     * @throws Exception
     */
    public Object generateObjectFromJson(InputStream inputStream, Metadata metadata) throws Exception {

        switch (metadata.getType()) {
            case TEXT:
                return IOUtils.convertStreamToString(inputStream, 100);
            case ITEM:
                return jsonHandler.unmarshalItem(metadata, inputStream);
            case ARRAY:
                return jsonHandler.unmarshalItemsAsStream(metadata, inputStream);
            default:
                throw new IllegalStateException("Unimplemented dataset type: " + metadata.getType());
        }
    }

//    /**
//     * Takes the object(s) and generates JSON and corresponding metadata.
//     *
//     * @param item The Object, Stream or Iterable to marshal to JSON.
//     * @param gzip Whether to gzip the stream. Usually this inputStream best as it reduces IO.
//     * @return the marshal results, with the metadata complete once the InputStream has been fully
//     * read.
//     * @throws IOException
//     * @deprecated Use JsonHandler directly
//     */
//    JsonMetadataPair generateJsonForItem(Object item, boolean gzip) throws IOException {
//        return jsonHandler.generateJsonForItem(item, gzip);
//
//    }

}
