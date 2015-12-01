package com.squonk.execution.variable.impl;

import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.execution.variable.VariableLoader;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class MemoryVariableLoader implements VariableLoader {

    protected final Map<String, Object> values = new LinkedHashMap<>();

    public MemoryVariableLoader() {

    }

    /**
     * Allows to create initial values for testing
     *
     * @param values
     */
    public MemoryVariableLoader(Map<String, Object> values) {
        this.values.putAll(values);
    }

    @Override
    public void save() {
        // noop
    }

    @Override
    public <V> V readFromText(String var, Class<V> type) throws IOException {
        String s = (String) values.get(var);
        if (s == null) {
            return null;
        } else {
            return convertFromText(s, type);
        }
    }

    private <V> V convertFromText(String s, Class<V> type) throws IOException {
        // TODO - use TypeConvertor mechanism and fall back to reflection
        try {
            Constructor constr = type.getConstructor(String.class);
            return (V) constr.newInstance(s);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IOException("Failed to construct value for type " + type.getName(), ex);
        }
    }

    @Override
    public <V> V readFromJson(String var, Class<V> type) throws IOException {
        String json = (String) values.get(var);
        if (json == null) {
            return null;
        } else {
            return JsonHandler.getInstance().objectFromJson(json, type);
        }
    }

    @Override
    public InputStream readBytes(String var, String label) throws IOException {
        byte[] bytes = (byte[]) values.get(var + label);
        if (bytes == null) {
            return null;
        } else {
            return new ByteArrayInputStream(bytes);
        }
    }

    @Override
    public void writeToText(String var, Object o) {
        values.put(var, o.toString());
    }

    @Override
    public void writeToJson(String var, Object o) throws IOException {
        String json = JsonHandler.getInstance().objectToJson(o);
        values.put(var, json);
    }

    @Override
    public void writeToBytes(String var, String label, InputStream is) throws IOException {
        byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
        values.put(var + label, bytes);
    }

    @Override
    public void delete(String var) {
        values.remove(var);
    }

}
