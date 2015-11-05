package com.im.lac.services.job.variable;

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
        // TODO - use TypeConvertor mechanism and fall back to reflection
        String s = (String) values.get(var);
        try {
            Constructor constr = type.getConstructor(String.class);
            return (V) constr.newInstance(s);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IOException("Failed to construct value", ex);
        }
    }

    @Override
    public <V> V readFromJson(String var, Class<V> type) throws IOException {
        String json = (String) values.get(var);
        return JsonHandler.getInstance().objectFromJson(json, type);
    }

    
    @Override
    public InputStream readFromBytes(String var) throws IOException {
        byte[] bytes = (byte[])values.get(var);
        return new ByteArrayInputStream(bytes);
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
    public void writeToBytes(String var, InputStream is) throws IOException {
        byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
        values.put(var, bytes);
    }

    @Override
    public void delete(String var) {
        values.remove(var);
    }
    
}
