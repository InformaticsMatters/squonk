/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core.client;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.squonk.client.NotebookVariableClient;
import org.squonk.core.config.SquonkClientConfig;
import org.squonk.notebook.api.NotebookCanvasDTO;
import org.squonk.notebook.api.NotebookDTO;
import org.squonk.notebook.api.NotebookEditableDTO;
import org.squonk.notebook.api.NotebookSavepointDTO;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import javax.enterprise.inject.Default;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
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

    public NotebookRestClient(String serverUrl) {
        this.baseUrl =  serverUrl + SquonkClientConfig.CORE_SERVICES_PATH + "/notebooks";
        LOG.info("NotebookRestClient is using base URL of " + baseUrl);
    }

    public NotebookRestClient() {
        this(SquonkClientConfig.CORE_SERVICES_SERVER);
    }

    @Override
    public NotebookDTO createNotebook(String username, String notebookName, String notebookDescription) throws IOException {
        LOG.fine("Creating notebook: POST " + baseUrl + "?user=" + username + "&name=" + notebookName);
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
        String path = baseUrl + "/" + notebookId;
        LOG.fine("Deleting notebook: DELETE " + path);
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(path);
        executeDelete(b);
        return true;
    }

    @Override
    public NotebookDTO updateNotebook(Long notebookId, String name, String description) throws IOException {
        String path = baseUrl + "/" + notebookId;
        LOG.fine("Updating notebook: PUT " + path + "?name=" + name);
        // PUT {notebookid}
        assert notebookId != null;
        assert name != null;
        URIBuilder b = new URIBuilder().setPath(path)
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
        LOG.fine("Listing notebooks: GET " + baseUrl + "?user=" + username);
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(baseUrl)
                .setParameter("user", username);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookDTO.class, true).collect(Collectors.toList());
        }
    }


    @Override
    public List<String> listLayers(Long notebookId) throws Exception {
        String path = baseUrl + "/" + notebookId + "/layer";
        LOG.fine("Listing layers: GET " + path);
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(path);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, String.class, true).collect(Collectors.toList());
        }
    }

    @Override
    public NotebookDTO addNotebookToLayer(Long notebookId, String layer) throws Exception {
        String path = baseUrl + "/" + notebookId + "/layer/" + layer;
        LOG.fine("Adding notebook to layer: POST " + path);
        assert notebookId != null;
        assert layer != null;
        LOG.fine("Adding notebook " + notebookId + " to layer " + layer);
        URIBuilder b = new URIBuilder().setPath(path);
        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookDTO.class);
        }
    }

    @Override
    public NotebookDTO removeNotebookFromLayer(Long notebookId, String layer) throws Exception {
        String path = baseUrl + "/" + notebookId + "/layer/" + layer;
        LOG.fine("Removing notebook: DELETE " + path);
        assert notebookId != null;
        assert layer != null;
        URIBuilder b = new URIBuilder().setPath(path);
        try (InputStream is = executeDeleteAsInputStream(b)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookDTO.class);
        }
    }

    /** Fetch the editables for a particular notebook that are owned by this user
     *
     * @param notebookId The ID of the notebook
     * @param username The username
     * @return
     */
    @Override
    public List<NotebookEditableDTO> listEditables(Long notebookId, String username) throws IOException {
        String path = baseUrl + "/" + notebookId + "/e";
        LOG.fine("Listing editables: GET " + path + "?user=" + username);
        assert notebookId != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(path).setParameter("user", username);
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
        String path = baseUrl + "/" + notebookId + "/s";
        LOG.fine("Listing savepoints: GET " + path);
        assert notebookId != null;
        URIBuilder b = new URIBuilder().setPath(path);
        try (InputStream is = executeGetAsInputStream(b)) {
            return JsonHandler.getInstance().streamFromJson(is, NotebookSavepointDTO.class, true).collect(Collectors.toList());
        }
    }

    @Override
    public NotebookEditableDTO createEditable(Long notebookId, Long parentId, String username) throws IOException {
        String path = baseUrl + "/" + notebookId + "/e";
        LOG.fine("Creating editable: POST " + path  + "?user=" + username+ "&parent=" + parentId);
        assert notebookId != null;
        assert username != null;
        // parent can be null
        URIBuilder b = new URIBuilder().setPath(path)
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
        String path = baseUrl + "/" + notebookId + "/e/" + editableId;
        LOG.fine("Updating editable: PUT " + path);
        String json = JsonHandler.getInstance().objectToJson(canvasDTO);
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(path);
        try (InputStream is = executePutAsInputStream(b, new StringEntity(json))) {
//            String result = IOUtils.convertStreamToString(is);
//            LOG.info("JSON: " + result);
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditableDTO.class);
        }
    }


    @Override
    public boolean deleteEditable(Long notebookId, Long editableId, String username) throws Exception {
        String path = baseUrl + "/" + notebookId + "/e/" + editableId;
        LOG.fine("Deleting editable: DELETE " + path + "?user=" + username);
        assert notebookId != null;
        assert editableId != null;
        assert username != null;
        URIBuilder b = new URIBuilder().setPath(path).setParameter("user", username);
        executeDelete(b, new NameValuePair[0]);
        return true;
    }

    @Override
    public NotebookEditableDTO createSavepoint(Long notebookId, Long editableId, String description) throws IOException {
        String path = baseUrl + "/" + notebookId + "/s";
        LOG.fine("Creating savepoint: POST " + path + "?editableid=" + editableId);
        assert notebookId != null;
        assert editableId != null;
        URIBuilder b = new URIBuilder().setPath(path)
                .setParameter("editableid", editableId.toString())
                .setParameter("description", description);

        try (InputStream is = executePostAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookEditableDTO.class);
        }
    }

    @Override
    public NotebookSavepointDTO setSavepointDescription(Long notebookId, Long savepointId, String description) throws IOException {
        String path = baseUrl + "/" + notebookId + "/s/" + savepointId + "/description";
        LOG.fine("Setting savepoint description: PUT " + path);
        // PUT {notebookid}/s/{savepointid}/description
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(path).setParameter("description", description);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepointDTO.class);
        }
    }

    @Override
    public NotebookSavepointDTO setSavepointLabel(Long notebookId, Long savepointId, String label) throws IOException {
        String path = baseUrl + "/" + notebookId + "/s/" + savepointId + "/label";
        LOG.fine("Setting label: PUT " + path);
        // PUT {notebookid}/s/{savepointid}/label
        assert notebookId != null;
        assert savepointId != null;
        URIBuilder b = new URIBuilder().setPath(path).setParameter("label", label);
        try (InputStream is = executePutAsInputStream(b, null)) {
            return JsonHandler.getInstance().objectFromJson(is, NotebookSavepointDTO.class);
        }
    }

    @Override
    public String readTextValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) throws IOException {
        String path = buildVariableUrl(notebookId, sourceId, cellId, variableName, key, VarType.t);
        LOG.fine("Reading text value: GET " + path);
        assert sourceId != null;
        assert variableName != null;
        URIBuilder b = createURIBuilder(path);
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
        String path = buildVariableUrl(notebookId, sourceId, cellId, variableName, key, VarType.s);
        LOG.fine("Reading stream value: GET " + path);
        assert sourceId != null;
        assert variableName != null;
        URIBuilder b = createURIBuilder(path);
        InputStream is = executeGetAsInputStream(b);

        LOG.finer("Reading stream value completed");
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
        String path = buildVariableUrl(notebookId, editableId, cellId, variableName, key, VarType.t);
        LOG.fine("Writing text value: POST " + path);
        assert editableId != null;
        assert cellId != null;
        assert variableName != null;
        URIBuilder b = createURIBuilder(path);
        executePost(b, new StringEntity(value));
    }

    @Override
    public void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value, String key) throws IOException {
        String path = buildVariableUrl(notebookId, editableId, cellId, variableName, key, VarType.s);
        LOG.fine("Writing stream value: POST " + path);
        assert value != null;
        assert editableId != null;
        assert cellId != null;
        assert variableName != null;
        try {
            URIBuilder b = createURIBuilder(path);
            executePost(b, new InputStreamEntity(value));
            LOG.finer("Writing stream value completed");
        } finally {
            value.close();
        }
    }

    @Override
    public void deleteVariable(Long notebookId, Long editableId, Long cellId, String variableName) throws IOException {
        String path =  baseUrl + "/" + notebookId + "/v/" + editableId + "/" + cellId + "/" + variableName;
        LOG.fine("Deleting variable: DELETE " + path);
        URIBuilder b = new URIBuilder().setPath(path);
        executeDelete(b);
        LOG.finer("deleteVariable completed");
    }


    private URIBuilder createURIBuilder(Long notebookId, Long sourceId, Long cellId, String variableName, String key, VarType t) {
        return new URIBuilder().setPath(buildVariableUrl(notebookId, sourceId, cellId, variableName, key, t));
    }

    private URIBuilder createURIBuilder(String path) {
        return new URIBuilder().setPath(path);
    }

    private String buildVariableUrl(Long notebookId, Long sourceId, Long cellId, String variableName, String key, VarType type) {
        return baseUrl + "/" + notebookId + "/v/" + sourceId + "/" + cellId + "/" + variableName + "/" + type.toString() + "/" + (key == null ? DEFAULT_KEY : key);
    }

}
