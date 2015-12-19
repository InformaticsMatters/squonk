package org.squonk.execution.variable.impl;

import org.squonk.execution.variable.VariableLoader;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import org.squonk.notebook.api.VariableKey;

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

    protected final Map<VariableKey, Object> values = new LinkedHashMap<>();

    public MemoryVariableLoader() {

    }

    /**
     * Allows to create initial values for testing
     *
     * @param values
     */
    public MemoryVariableLoader(Map<VariableKey, Object> values) {
        this.values.putAll(values);
    }

    @Override
    public void save() {
        // noop
    }

    @Override
    public <V> V readFromText(VariableKey var, Class<V> type) throws IOException {
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
    public <V> V readFromJson(VariableKey var, Class<V> type) throws IOException {
        String json = (String) values.get(var);
        if (json == null) {
            return null;
        } else {
            return JsonHandler.getInstance().objectFromJson(json, type);
        }
    }

    @Override
    public InputStream readBytes(VariableKey var, String label) throws IOException {
        VariableKey neu = new VariableKey(var.getProducerName(), var.getName() + label);
        byte[] bytes = (byte[]) values.get(neu);
        if (bytes == null) {
            return null;
        } else {
            return new ByteArrayInputStream(bytes);
        }
    }

    @Override
    public void writeToText(VariableKey var, Object o) {
        values.put(var, o.toString());
    }

    @Override
    public void writeToJson(VariableKey var, Object o) throws IOException {
        String json = JsonHandler.getInstance().objectToJson(o);
        values.put(var, json);
    }

    @Override
    public void writeToBytes(VariableKey var, String label, InputStream is) throws IOException {
        byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
        VariableKey neu = new VariableKey(var.getProducerName(), var.getName() + label);
        values.put(neu, bytes);
    }

}
