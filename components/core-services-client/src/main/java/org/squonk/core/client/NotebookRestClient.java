package org.squonk.core.client;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.squonk.client.NotebookVariableClient;
import org.squonk.core.client.config.SquonkClientConfig;
import org.squonk.notebook.api.*;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import javax.enterprise.inject.Default;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 11/03/16.
 */
@Default
public class NotebookRestClient extends AbstractHttpClient implements Serializable, NotebookVariableClient {

    private static final Logger LOG = Logger.getLogger(NotebookRestClient.class.getName());

    private final String baseUrl;

    public NotebookRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
        LOG.info("NotebookRestClient is using base URL of " + baseUrl);
    }

    public NotebookRestClient() {
        this(SquonkClientConfig.INSTANCE.getCoreServiceBaseUrl() + "/notebooks");
    }

    @Override
    public NotebookDTO createNotebook(String username, String notebookName, String notebookDescription) throws IOException {
        assert notebookName != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl)
                .setParameter("user", username)
                .setParameter("name", notebookName);
        if (notebookDescription != null) {
            b = b.setParameter("description", notebookDescription);
        }
        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookDTO.class);
        }
    }

    @Override
    public boolean deleteNotebook(Long notebookId) throws Exception {
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId);
        executeDelete(b);
        return true;
    }

    @Override
    public NotebookDTO updateNotebook(Long notebookId, String name, String description) throws IOException {
        // PUT {notebookid}
        assert notebookId != null;
        assert name != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId)
                .setParameter("name", name);
        if (description != null) {
            b = b.setParameter("description", description);
        }
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookDTO.class);
        }
    }

    /** Fetch the notebooks this user has access to
     *
     * @param username The username
     * @return
     */
    @Override
    public List<NotebookDTO> listNotebooks(String username) throws IOException {
        assert username != null;
        LOG.info("Listing notebooks for user " + username);
        URIBuilder b = new URIBuilder().setPath(baseUrl)
                .setParameter("user", username);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookDTO.class, true).collect(Collectors.toList());
        }
    }


    @Override
    public List<String> listLayers(Long notebookId) throws Exception {
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/layer");
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, String.class, true).collect(Collectors.toList());
        }
    }

    @Override
    public void addNotebookToLayer(Long notebookId, String layer) throws Exception {
        assert notebookId != null;
        assert layer != null;
        LOG.info("Adding notebook " + notebookId + " to layer " + layer);
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/layer/" + layer);
        executePost(b, null);
    }

    @Override
    public void removeNotebookFromLayer(Long notebookId, String layer) throws Exception {
        assert notebookId != null;
        assert layer != null;
        LOG.info("Removing notebook " + notebookId + " from layer " + layer);
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/layer/" + layer);
        executeDelete(b);
    }

    /** Fetch the editables for a particular notebook that are owned by this user
     *
     * @param notebookId The ID of the notebook
     * @param username The username
     * @return
     */
    @Override
    public List<NotebookEditableDTO> listEditables(Long notebookId, String username) throws IOException {
        assert notebookId != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/e")
                .setParameter("user", username);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookEditableDTO.class, true).collect(Collectors.toList());
        }
    }

    /** Fetch the savepoints for a particular notebook
     *
     * @param notebookId The ID of the notebook
     * @return
     */
    @Override
    public List<NotebookSavepointDTO> listSavepoints(Long notebookId) throws IOException {
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s");
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookSavepointDTO.class, true).collect(Collectors.toList());
        }
    }

    @Override
    public NotebookEditableDTO createEditable(Long notebookId, Long parentId, String username) throws IOException {
        assert notebookId != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/e")
                .setParameter("user", username);
                if (parentId != null) {
                    b = b.setParameter("parent", parentId.toString());
                }

        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditableDTO.class);
        }
    }

    @Override
    public NotebookEditableDTO updateEditable(Long notebookId, Long editableId, NotebookCanvasDTO canvasDTO) throws IOException {
        // PUT {notebookid}/e/{editableid}
        String json = JsonHandler.getInstance().objectToJson(canvasDTO);
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/e/" + editableId);
        try (InputStream is = executePutAsInputStream(b, new StringEntity(json))) {
//            String result = IOUtils.convertStreamToString(is);
//            LOG.info("JSON: " + result);
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditableDTO.class);
        }
    }

    @Override
    public NotebookEditableDTO createSavepoint(Long notebookId, Long editableId) throws IOException {
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s")
                .setParameter("editableid", editableId.toString());

        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditableDTO.class);
        }
    }

    @Override
    public NotebookSavepointDTO setSavepointDescription(Long notebookId, Long savepointId, String description) throws IOException {
        // PUT {notebookid}/s/{savepointid}/description
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s/" + savepointId + "/description")
                .setParameter("description", description);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepointDTO.class);
        }
    }

    @Override
    public NotebookSavepointDTO setSavepointLabel(Long notebookId, Long savepointId, String label) throws IOException {
        // PUT {notebookid}/s/{savepointid}/label
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s/" + savepointId + "/label")
                .setParameter("label", label);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepointDTO.class);
        }
    }

    @Override
    public String readTextValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) throws IOException {
        LOG.finer("readTextValue " + buildVariableUrl(notebookId, sourceId, cellId, variableName, key, VarType.t));
        assert sourceId != null;
        assert variableName != null;
        URIBuilder b = createURIBuilder(notebookId, sourceId, cellId, variableName, key, VarType.t);
        try (InputStream is = executeGetAsInputStream(b)) {
            LOG.finer("readTextValue completed");
            return is == null ? null : IOUtils.convertStreamToString(is);
        }
    }

//    public URL getTextValueURL(Long notebookId, Long sourceId, String variableName, String key) {
//        try {
//            return createURIBuilder(notebookId, sourceId, variableName, key, VarType.t).build().toURL();
//        } catch (MalformedURLException | URISyntaxException e) {
//            throw new IllegalArgumentException("Unable to build URL. Maybe bad parameters?");
//        }
//    }

//    @Override
//    public String readTextValue(Long notebookId, String label, String variableName, String key) throws IOException {
//        assert notebookId != null;
//        assert label != null;
//        assert variableName != null;
//        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.t))
//                .setParameter("label", label);
//        try (InputStream is = executeGetAsInputStream(b)) {
//            String s = IOUtils.convertStreamToString(is, 1000);
//            return s;
//        }
//    }

    @Override
    public InputStream readStreamValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) throws IOException {
        LOG.finer("readStreamValue " + buildVariableUrl(notebookId, sourceId, cellId, variableName, key, VarType.s));
        assert sourceId != null;
        assert variableName != null;
        URIBuilder b = createURIBuilder(notebookId, sourceId, cellId, variableName, key, VarType.s);
        InputStream is = executeGetAsInputStream(b);
        LOG.finer("readStreamValue completed");
        return is;
    }

//    public URL getStreamValueURL(Long notebookId, Long sourceId, String variableName, String key) {
//        try {
//            return createURIBuilder(notebookId, sourceId, variableName, key, VarType.s).build().toURL();
//        } catch (MalformedURLException | URISyntaxException e) {
//            throw new IllegalArgumentException("Unable to build URL. Maybe bad parameters?");
//        }
//    }

//    @Override
//    public InputStream readStreamValue(Long notebookId, String label, String variableName, String key) throws IOException {
//        assert notebookId != null;
//        assert label != null;
//        assert variableName != null;
//        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.s))
//                .setParameter("label", label);
//        InputStream is = executeGetAsInputStream(b);
//        return is;
//    }

    @Override
    public void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value, String key) throws IOException {
        assert editableId != null;
        assert cellId != null;
        assert variableName != null;
        URIBuilder b = createURIBuilder(notebookId, editableId, cellId, variableName, key, VarType.t);
        executePost(b, new StringEntity(value));
    }

    @Override
    public void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value, String key) throws IOException {
        assert editableId != null;
        assert cellId != null;
        assert variableName != null;
        URIBuilder b = createURIBuilder(notebookId, editableId, cellId, variableName, key, VarType.s);
        executePost(b, new InputStreamEntity(value));
    }

    private URIBuilder createURIBuilder(Long notebookId, Long sourceId, Long cellId, String variableName, String key, VarType t) {
        return new URIBuilder().setPath(buildVariableUrl(notebookId, sourceId, cellId, variableName, key, t));
    }

    private String buildVariableUrl(Long notebookId, Long sourceId, Long cellId, String variableName, String key, VarType type) {
        return baseUrl + "/" + notebookId + "/v/" + sourceId + "/" + cellId + "/" + variableName + "/" + type.toString() + "/" + (key == null ? DEFAULT_KEY : key);
    }

}
