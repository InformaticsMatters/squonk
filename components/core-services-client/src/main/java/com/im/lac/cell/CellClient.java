package com.im.lac.cell;

import com.im.lac.client.AbstractHttpClient;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.util.IOUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by timbo on 31/12/15.
 */
@ApplicationScoped
public class CellClient extends AbstractHttpClient {

    private static final Logger LOG = Logger.getLogger(CellClient.class.getName());

    private static final String PARAM_NOTEBOOK_ID = "notebookId";
    private static final String PARAM_CELL_NAME = "cellName";
    private static final String PARAM_PRODUCER_NAME = "producerName";
    private static final String PARAM_VARIABLE_NAME = "variableName";
    private static final String PARAM_VALUE = "value";

    private final String baseUrl = IOUtils.getConfiguration("SERVICE_CALLBACK", "http://localhost:8080/ws/notebook");
    private final Long notebookId;

    public CellClient(Long notebookId) {
        this.notebookId = notebookId;
        LOG.info("CellClient is using base URL of " + baseUrl);
    }

    public NotebookDTO retrieveNotebookDefinition() throws IOException {
        LOG.info("retrieveNotebookDefinition " + notebookId);
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/retrieveNotebook")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString());
        String s = executeGetAsString(b);
        return fromJson(s, NotebookDTO.class);
    }

    public CellDTO retrieveCell(String cellName) throws IOException {
        LOG.info("retrieveCell " + cellName);
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/retrieveCell")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_CELL_NAME, cellName);
        String s = executeGetAsString(b);
        return fromJson(s, CellDTO.class);
    }

    public String readTextValue(String producerName, String variableName) throws IOException {
        LOG.info("readTextValue " + producerName + ":" + variableName);
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/readTextValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName);
        return executeGetAsString(b);
    }

    public Integer readIntegerValue(String producerName, String variableName) throws IOException {
        LOG.info("readIntegerValue " + producerName + ":" + variableName);
        String s = readTextValue(producerName, variableName);
        return s == null ? null : new Integer(s);
    }

    /** Get the content as an InputStream which MUST be closed when finished with
     *
     * @param producerName
     * @param variableName
     * @return
     */
    public InputStream readStreamValue(String producerName, String variableName) throws IOException {
        LOG.info("readStreamValue " + producerName + ":" + variableName);
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/readStreamValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName);
        return executeGetAsInputStream(b);
    }

    public void writeTextValue(String producerName, String variableName, String value) throws IOException {
        LOG.info("writeTextValue " + producerName + ":" + variableName);
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/writeTextValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName)
                .setParameter(PARAM_VALUE, value);
        // TODO - the value should be POSTed, not set as query param?
        executePost(b, (AbstractHttpEntity) null, new NameValuePair[0]);
    }

    public void writeIntegerValue(String producerName, String variableName, Integer value) throws IOException {
        LOG.info("writeIntegerValue " + producerName + ":" + variableName);
        // TODO - is this method really needed? Shouldn't it be handled as text?
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/writIntegerValue")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName)
                .setParameter(PARAM_VALUE, value == null ? null : value.toString());
        // TODO - the value should be POSTed, not set as query param?
        executePost(b, (AbstractHttpEntity) null);
    }

    public void writeStreamContents(String producerName, String variableName, InputStream inputStream) throws IOException {
        LOG.info("writeStreamContents " + producerName + ":" + variableName);
        URIBuilder b = new URIBuilder()
                .setPath(baseUrl + "/writeStreamContents")
                .setParameter(PARAM_NOTEBOOK_ID, notebookId.toString())
                .setParameter(PARAM_PRODUCER_NAME, producerName)
                .setParameter(PARAM_VARIABLE_NAME, variableName);
        executePost(b, new InputStreamEntity(inputStream));
    }
}
