package org.squonk.core.client;

import com.im.lac.client.AbstractHttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
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
public class NotebookRestClient extends AbstractHttpClient //implements NotebookClient
{

    private static final Logger LOG = Logger.getLogger(NotebookRestClient.class.getName());

    private final String baseUrl;

    public NotebookRestClient(String baseUrl) {
        this.baseUrl = baseUrl;
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

    NotebookDescriptor updateNotebook(Long notebookId, String name, String description) throws IOException {
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
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/editables")
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
    List<NotebookSavepoint> listSavepoints(Long notebookId) throws IOException {
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/savepoints");
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookSavepoint.class, true).collect(Collectors.toList());
        }
    }

    public NotebookEditable createEditable(Long notebookId, Long parentId, String username) throws IOException {
        assert notebookId != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/editables")
                .setParameter("user", username);
                if (parentId != null) {
                    b = b.setParameter("parent", parentId.toString());
                }

        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditable.class);
        }
    }


    public NotebookEditable updateEditable(Long notebookId, Long editableId, String json) throws IOException {
        // PUT {notebookid}/editables/{editableid}
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/editables/" + editableId);
        try (InputStream is = executePutAsInputStream(b, new StringEntity(json))) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditable.class);
        }
    }

    public NotebookEditable createSavepoint(Long notebookId, Long editableId) throws IOException {
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/savepoints")
                .setParameter("editableid", editableId.toString());

        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditable.class);
        }
    }

    public NotebookSavepoint setSavepointDescription(Long notebookId, Long savepointId, String description) throws IOException {
        // PUT {notebookid}/savepoints/{savepointid}/description
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/savepoints/" + savepointId + "/description")
                .setParameter("description", description);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepoint.class);
        }
    }

    public NotebookSavepoint setSavepointLabel(Long notebookId, Long savepointId, String label) throws IOException {
        // PUT {notebookid}/savepoints/{savepointid}/label
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl + "/" + notebookId + "/savepoints/" + savepointId + "/label")
                .setParameter("label", label);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepoint.class);
        }
    }


}
