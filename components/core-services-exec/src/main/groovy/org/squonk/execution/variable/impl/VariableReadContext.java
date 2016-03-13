package org.squonk.execution.variable.impl;

import org.squonk.core.Variable;
import org.squonk.core.client.NotebookRestClient;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by timbo on 13/03/16.
 */
public class VariableReadContext implements Variable.ReadContext{

    private final NotebookRestClient client;
    private final Long notebookId;
    private final Long sourceId;
    private final String variableName;

    public VariableReadContext(NotebookRestClient client, Long notebookId, Long sourceId, String variableName) {
        this.client = client;
        this.notebookId = notebookId;
        this.sourceId = sourceId;
        this.variableName = variableName;
    }

    @Override
    public String readTextValue(String key) throws IOException {
        return client.readTextValue(notebookId, sourceId, variableName, key);
    }

    @Override
    public InputStream readStreamValue(String key) throws IOException {
        return client.readStreamValue(notebookId, sourceId, variableName, key);
    }

    @Override
    public URL getTextValueUrl(String key) {
        return client.getTextValueURL(notebookId, sourceId, variableName, key);
    }

    @Override
    public URL getStreamValueUrl(String key) {
        return client.getStreamValueURL(notebookId, sourceId, variableName, key);
    }

    @Override
    public String readTextValue(URL url) throws IOException {
        // TODO - add headers for authenticated use
        return IOUtils.convertStreamToString(url.openStream());
    }

    @Override
    public InputStream readStreamValue(URL url) throws IOException {
        // TODO - add headers for authenticated use
        return url.openStream();
    }
}
