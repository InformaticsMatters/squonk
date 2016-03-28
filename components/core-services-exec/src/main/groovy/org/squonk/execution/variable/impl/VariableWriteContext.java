package org.squonk.execution.variable.impl;

import org.squonk.api.VariableHandler;
import org.squonk.core.client.NotebookRestClient;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 13/03/16.
 */
public class VariableWriteContext implements VariableHandler.WriteContext {

    private final NotebookRestClient client;
    private final Long notebookId;
    private final Long editableId;
    private final Long cellId;
    private final String variableName;

    public VariableWriteContext(NotebookRestClient client, Long notebookId, Long editableId, Long cellId, String variableName) {
        this.client = client;
        this.notebookId = notebookId;
        this.editableId = editableId;
        this.cellId = cellId;
        this.variableName = variableName;
    }

    @Override
    public void writeTextValue(String val, String key) throws IOException {
        client.writeTextValue(notebookId, editableId, cellId, variableName, val, key);
    }

    @Override
    public void writeStreamValue(InputStream val, String key) throws IOException {
        client.writeStreamValue(notebookId, editableId, cellId, variableName, IOUtils.getGzippedInputStream(val), key);
    }

}
