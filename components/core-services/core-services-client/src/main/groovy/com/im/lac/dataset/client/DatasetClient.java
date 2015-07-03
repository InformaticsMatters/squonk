package com.im.lac.dataset.client;

import com.im.lac.dataset.DataItem;
import com.im.lac.types.io.JsonHandler;
import com.im.lac.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Client for datasets. Implementation user Apache HTTP client for REST calls and Jackson for JSON
 * marshaling/unmarshaling. JSON handling specifically handles any custom marshalers/unmarshalers
 * that are defined.
 *
 * Implementation note from Apache HTTP client: The underlying HTTP connection is still held by the
 * response object to allow the response content to be streamed directly from the network socket. In
 * order to ensure correct deallocation of system resources the user MUST call
 * CloseableHttpResponse#close() from a finally clause. Please note that if response content is not
 * fully consumed the underlying connection cannot be safely re-used and will be shut down and
 * discarded by the connection manager.
 *
 * @author timbo
 */
public class DatasetClient {

    private static final Logger LOG = Logger.getLogger(DatasetClient.class.getName());
    private static final String DEFAULT_BASE_URL = "http://demos.informaticsmatters.com:8080/dataset-service/rest/v1/datasets";

    private final String base;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final JsonHandler jsonHandler = new JsonHandler();

    public DatasetClient(String baseUrl) {
        this.base = baseUrl;
    }

    public DatasetClient() {
        base = DEFAULT_BASE_URL;
    }

    /**
     * Get all the DataItems of the dataset that are present
     *
     * @return Stream of the DataItems
     * @throws IOException
     */
    public Stream<DataItem> getAll() throws IOException {

        HttpGet httpGet = new HttpGet(base);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        LOG.fine(response.getStatusLine().toString());
        HttpEntity entity1 = response.getEntity();
        InputStream is = entity1.getContent();
        return jsonHandler.streamFromJson(is, DataItem.class, true);
    }

    /**
     * Create a new dataset Note: currently this assumes that the content is molecules in some
     * format that can be parsed. In future we will need to extend this to allow a parser to be
     * specified that allows the content to be converted into a Stream of whatever objects are
     * supported.
     *
     * @param name the name for the new dataset
     * @param content The contents
     * @return A description of the new dataset
     * @throws java.io.IOException
     */
    public DataItem create(String name, InputStream content) throws IOException {
        HttpPost httpPost = new HttpPost(base);
        httpPost.setHeader(DataItem.HEADER_DATA_ITEM_NAME, name);
        httpPost.setEntity(new InputStreamEntity(content));
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            return jsonHandler.objectFromJson(is, DataItem.class);
        }
    }

    /**
     * Get a descriptor of the specified dataset
     *
     * @param id The dataset ID
     * @return
     * @throws java.io.IOException
     */
    public DataItem get(Long id) throws IOException {
        HttpGet httpGet = new HttpGet(base + "/" + id + "/dataitem");
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            return jsonHandler.objectFromJson(is, DataItem.class);
        }
    }

    /**
     * Delete the specified dataset
     *
     * @param id The dataset ID
     * @return The HTTP status code. Hopefully 200.
     * @throws java.io.IOException
     */
    public int delete(Long id) throws IOException {
        HttpDelete httpDelete = new HttpDelete(base + "/" + id);
        try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
            LOG.fine(response.getStatusLine().toString());
            return response.getStatusLine().getStatusCode();
        }
    }

// will be supported once we need to handle different (single) bject types   
//    public <T> Object getContents(DataItem item, Class<T> type) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//    /**
//     * Need some way to get the raw byte stream, but not sure how to close the response. 
//     */
//    public InputStream getRawContents(DataItem item) throws IOException {
//        
//    }
    /**
     * Get the contents of the dataset
     * <p>
     * Note: the type parameter is defined in the DataItem metadata property - possibly we could
     * replace this by handling DataItem as a generic type (but see next note).
     * <p>
 Note: it is deliberate that this method takes a Class as its type argument whilst the
 Metadata class handles the class name as a string. This because we cannot guarantee that the
 client will have access to all the classes that the server can supply as JSON. If not then
 its an error, but this approach at least allows the client to fail gracefully. See the (
 private getContentsAsObjects() to get an idea of what the problem is (the need to throw a
 ClassNotFoundException).
     *
     * @param <T> The type of object to be created
     * @param item Descriptor of the dataset
     * @param type The type of object
     * @return An Stream of objects for the dataset
     * @throws java.io.IOException
     */
    public <T> Stream<T> getContentsAsObjects(DataItem item, Class<T> type) throws IOException {
        HttpGet httpGet = new HttpGet(base + "/" + item.getId() + "/content");
        final CloseableHttpResponse response = httpclient.execute(httpGet);
        LOG.fine(response.getStatusLine().toString());
        HttpEntity entity1 = response.getEntity();
        InputStream is = entity1.getContent();
        Stream<T> stream = jsonHandler.streamFromJson(is, type, item.getMetadata(), false);
        return stream.onClose(() -> {
            IOUtils.close(is);
            IOUtils.close(response);
        });
    }

    private Stream getContentsAsObjects(DataItem item) throws ClassNotFoundException, IOException {
        Class cls = Class.forName(item.getMetadata().getClassName());
        return getContentsAsObjects(item, cls);
    }

}
