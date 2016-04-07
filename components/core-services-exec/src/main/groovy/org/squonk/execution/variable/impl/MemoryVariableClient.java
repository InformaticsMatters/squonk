package org.squonk.execution.variable.impl;

import org.squonk.client.VariableClient;
import org.squonk.execution.variable.VariableLoader;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import org.squonk.notebook.api.VariableKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class MemoryVariableClient implements VariableClient {

    private static final Logger LOG = Logger.getLogger(MemoryVariableClient.class.getName());

    protected final Map<String, String> stringValues = new HashMap<>();
    protected final Map<String, byte[]> streamValues = new HashMap<>();

    public MemoryVariableClient() {

    }

    /**
     * Allows to submit initial values for testing
     *
     */
    public MemoryVariableClient(Map<String, String> stringValues, Map<String, byte[]> streamValues) {
        this.streamValues.putAll(streamValues);
        this.stringValues.putAll(stringValues);
    }


    @Override
    public String readTextValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) {
        String k = generateKey(notebookId, sourceId, cellId, variableName, key);
        String s = stringValues.get(k);
        return stringValues.get(k);
    }

//    @Override
//    public String readTextValue(Long notebookId, String label, String variableName, String key) {
//        return null;
//    }


    public void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value, String key) {
        String k = generateKey(notebookId, editableId, cellId, variableName, key);
        stringValues.put(k, value);
    }

    public InputStream readStreamValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) {
        String k = generateKey(notebookId, sourceId, cellId, variableName, key);

        byte[] bytes = streamValues.get(k);
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

//    public InputStream readStreamValue(Long notebookId, String label, String variableName, String key) {
//        return null;
//    }

    public void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value, String key) throws IOException {
        String k = generateKey(notebookId, editableId, cellId, variableName, key);
        byte[] bytes = IOUtils.convertStreamToBytes(value, 1000);
        streamValues.put(k, bytes);
    }

    public String generateKey(Long notebookId, Long sourceId, Long cellId, String variableName, String key) {
        String s = notebookId + "#" + sourceId + "#" + cellId + "#" + variableName;
        return (key == null ? s : s + "#" + key);
    }


}
