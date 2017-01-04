package org.squonk.execution.variable.impl;

import org.squonk.api.VariableHandler;
import org.squonk.client.VariableClient;
import org.squonk.core.client.NotebookRestClient;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 13/03/16.
 */
public class VariableWriteContext implements VariableHandler.WriteContext {

    private final VariableClient client;
    private final Long notebookId;
    private final Long editableId;
    private final Long cellId;
    private final String variableName;

    public VariableWriteContext(VariableClient client, Long notebookId, Long editableId, Long cellId, String variableName) {
        this.client = client;
        this.notebookId = notebookId;
        this.editableId = editableId;
        this.cellId = cellId;
        this.variableName = variableName;
    }

    @Override
    public void writeTextValue(String value, String key) throws Exception {
        client.writeTextValue(notebookId, editableId, cellId, variableName, value, key);
    }

    @Override
    public void writeSingleTextValue(String value, String key) throws Exception {
        writeTextValue(value, null);
    }

    @Override
    public void writeStreamValue(InputStream value, String key) throws Exception {
        client.writeStreamValue(notebookId, editableId, cellId, variableName, IOUtils.getGzippedInputStream(value), key);
    }

    @Override
    public void writeSingleStreamValue(InputStream value, String key) throws Exception {
        writeStreamValue(value, null);
    }

    @Override
    public void deleteVariable() throws Exception {

    }


}
