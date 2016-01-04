package org.squonk.notebook.client;

import com.im.lac.client.AbstractHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 31/12/15.
 */
public class TmpNotebookCellClient extends AbstractHttpClient {

    private static final Logger LOG = Logger.getLogger(TmpNotebookCellClient.class.getName());

    private static final String PARAM_NOTEBOOK_ID = "notebookId";
    private static final String PARAM_PRODUCER_NAME = "cellName";
    private static final String PARAM_VARIABLE_NAME = "variableName";
    private static final String PARAM_VALUE = "alue";

    private final String baseUrl;

    private final Long notebookId;

    public TmpNotebookCellClient(Long notebookId, String baseUrl) {
        this.notebookId = notebookId;
        this.baseUrl = baseUrl;
    }

    public NotebookDTO retrieveNotebookDefinition() {
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/retrieveNotebook")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString());
        String s = executeGetAsString(b);
        return fromJson(s, NotebookDTO.class);
    }

    public CellDTO retrieveCell(String cellName) {
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/retrieveCell")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, cellName);
        String s = executeGetAsString(b);
        return fromJson(s, CellDTO.class);
    }

    public String readTextValue(String producerName, String variableName) {
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/readTextValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName);
        return executeGetAsString(b);
    }

    public Integer readIntegerValue(String producerName, String variableName) {
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/readTextValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName);
        String s = executeGetAsString(b);
        return s == null ? null : new Integer(s);
    }

    /** Get the content as an InputStream which MUST be closed when finished with
     *
     * @param producerName
     * @param variableName
     * @return
     */
    public InputStream readStreamValue(String producerName, String variableName) {
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/readStreamValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName);
        return executeGetAsInputStream(b);
    }

    public void writeTextValue(String producerName, String variableName, String value) {
//        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
//        queryParams.add("notebookId", context.getNotebookId().toString());
//        queryParams.add("producerName", producerName);
//        queryParams.add("variableName", variableName);
//        queryParams.add("value", value);
//        newResourceBuilder("/writeTextValue", queryParams).post();

        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/writeTextValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName)
                .setParameter(PARAM_VALUE, value);
        // TODO - the value should be POSTed, not set as query param


    }
}
