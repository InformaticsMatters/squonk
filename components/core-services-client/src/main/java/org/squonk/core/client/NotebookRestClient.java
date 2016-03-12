package org.squonk.core.client;

import com.im.lac.client.AbstractHttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.squonk.client.NotebookClient;
import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.api2.NotebookSavepoint;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 11/03/16.
 */
public class NotebookRestClient extends AbstractHttpClient implements NotebookClient
{

    private static final Logger LOG = Logger.getLogger(NotebookRestClient.class.getName());

    private final String baseUrl;

    public NotebookRestClient(String baseUrl) {
        this.baseUrl = baseUrl + "/notebooks";
    }

    public NotebookRestClient() {
        this.baseUrl = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://localhost/coreservices/rest/v1") + "/notebooks";
        LOG.info("NotebookRestClient is using base URL of " + baseUrl);
    }

    public NotebookDescriptor createNotebook(String username, String notebookName, String notebookDescription) throws IOException {
        assert notebookName != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl)
                .setParameter("user", username)
                .setParameter("name", notebookName);
        if (notebookDescription != null) {
            b = b.setParameter("description", notebookDescription);
        }
        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookDescriptor.class);
        }
    }

    public NotebookDescriptor updateNotebook(Long notebookId, String name, String description) throws IOException {
        // PUT {notebookid}
        assert notebookId != null;
        assert name != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId)
                .setParameter("name", name);
        if (description != null) {
            b = b.setParameter("description", description);
        }
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookDescriptor.class);
        }
    }

    /** Fetch the notebooks this user has access to
     *
     * @param username The username
     * @return
     */
    public List<NotebookDescriptor> listNotebooks(String username) throws IOException {
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl)
                .setParameter("user", username);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookDescriptor.class, true).collect(Collectors.toList());
        }
    }

    /** Fetch the editables for a particular notebook that are owned by this user
     *
     * @param notebookId The ID of the notebook
     * @param username The username
     * @return
     */
    public List<NotebookEditable> listEditables(Long notebookId, String username) throws IOException {
        assert notebookId != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/e")
                .setParameter("user", username);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookEditable.class, true).collect(Collectors.toList());
        }
    }

    /** Fetch the savepoints for a particular notebook
     *
     * @param notebookId The ID of the notebook
     * @return
     */
    public List<NotebookSavepoint> listSavepoints(Long notebookId) throws IOException {
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s");
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookSavepoint.class, true).collect(Collectors.toList());
        }
    }

    public NotebookEditable createEditable(Long notebookId, Long parentId, String username) throws IOException {
        assert notebookId != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/e")
                .setParameter("user", username);
                if (parentId != null) {
                    b = b.setParameter("parent", parentId.toString());
                }

        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditable.class);
        }
    }


    public NotebookEditable updateEditable(Long notebookId, Long editableId, String json) throws IOException {
        // PUT {notebookid}/e/{editableid}
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/e/" + editableId);
        try (InputStream is = executePutAsInputStream(b, new StringEntity(json))) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditable.class);
        }
    }

    public NotebookEditable createSavepoint(Long notebookId, Long editableId) throws IOException {
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s")
                .setParameter("editableid", editableId.toString());

        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditable.class);
        }
    }

    public NotebookSavepoint setSavepointDescription(Long notebookId, Long savepointId, String description) throws IOException {
        // PUT {notebookid}/s/{savepointid}/description
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s/" + savepointId + "/description")
                .setParameter("description", description);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepoint.class);
        }
    }

    public NotebookSavepoint setSavepointLabel(Long notebookId, Long savepointId, String label) throws IOException {
        // PUT {notebookid}/s/{savepointid}/label
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/s/" + savepointId + "/label")
                .setParameter("label", label);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepoint.class);
        }
    }

    public String readTextValue(Long notebookId, Long sourceId, String variableName, String key) throws IOException {
        assert sourceId != null;
        assert variableName != null;
        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.t))
                .setParameter("sourceid", sourceId.toString());
        try (InputStream is = executeGetAsInputStream(b)) {
            return IOUtils.convertStreamToString(is, 1000);
        }
    }



    public String readTextValue(Long notebookId, String label, String variableName, String key) throws IOException {
        assert notebookId != null;
        assert label != null;
        assert variableName != null;
        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.t))
                .setParameter("label", label);
        try (InputStream is = executeGetAsInputStream(b)) {
            String s = IOUtils.convertStreamToString(is, 1000);
            return s;
        }
    }

    public InputStream readStreamValue(Long notebookId, Long sourceId, String variableName, String key) throws IOException {
        assert sourceId != null;
        assert variableName != null;
        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.s))
                .setParameter("sourceid", sourceId.toString());
        InputStream is = executeGetAsInputStream(b);
        return is;

    }

    public InputStream readStreamValue(Long notebookId, String label, String variableName, String key) throws IOException {
        assert notebookId != null;
        assert label != null;
        assert variableName != null;
        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.s))
                .setParameter("label", label);
        InputStream is = executeGetAsInputStream(b);
        return is;
    }

    public void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value, String key) throws IOException {
        assert editableId != null;
        assert cellId != null;
        assert variableName != null;
        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.t))
                .setParameter("editableid", editableId.toString())
                .setParameter("cellId", cellId.toString());
        executePost(b, new StringEntity(value));
    }

    public void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value, String key) throws IOException {
        assert editableId != null;
        assert cellId != null;
        assert variableName != null;
        URIBuilder b = new URIBuilder().setPath(buildVariableUrl(notebookId, variableName, key, VarType.s))
                .setParameter("editableid", editableId.toString())
                .setParameter("cellId", cellId.toString());
        executePost(b, new InputStreamEntity(value));
    }


    private String buildVariableUrl(Long notebookId, String variableName, String key, VarType type) {
        return baseUrl + "/" + notebookId + "/v/" + variableName + "/" + type.toString() + (key == null ? "" : "/" + key);
    }

}
