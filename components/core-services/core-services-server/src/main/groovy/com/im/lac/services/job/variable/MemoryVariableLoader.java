package com.im.lac.services.job.variable;

import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author timbo
 */
public class MemoryVariableLoader implements VariableLoader {

    private final Map<Variable, Object> values = new HashMap<>();
    
    public MemoryVariableLoader() {
        
    }
    
    /**
     * Allows to create initial values for testing
     * @param values 
     */
    public MemoryVariableLoader(Map<Variable, Object> values) {
        this.values.putAll(values);
    }
    
    @Override
    public Set<Variable> getVariables() {
        return values.keySet();
    }

    /**
     * Removes non persistent values.
     */
    @Override
    public void save() {
        Iterator<Map.Entry<Variable, Object>> it = values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Variable, Object> e = it.next();
            if (e.getKey().getPersistenceType() == Variable.PersistenceType.NONE) {
                it.remove();
            }
        }
    }

    @Override
    public <V> V loadVariable(Variable<V> var) throws IOException {
        switch (var.getPersistenceType()) {
            case TEXT:
                return readAsText(var);
            case JSON:
                return readAsJson(var);
            case BYTES:
                return readAsInputStream(var);
            case NONE:
                // return value as is
                return (V) values.get(var);
            default:
                throw new IllegalStateException("Unexpected persistence type: " + var.getPersistenceType());
        }
    }

    private <V> V readAsText(Variable<V> var) throws IOException {
        // TODO - use TypeConvertor mechanism and fall back to reflection
        String s = (String) values.get(var);
        try {
            Constructor constr = var.getType().getConstructor(String.class);
            return (V) constr.newInstance(s);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IOException("Failed to construct value", ex);
        }
    }

    private <V> V readAsJson(Variable<V> var) throws IOException {
        String json = (String) values.get(var);
        return JsonHandler.getInstance().objectFromJson(json, var.getType());
    }

    
    private <V> V readAsInputStream(Variable<V> var) throws IOException {
        byte[] bytes = (byte[])values.get(var);
        return (V) new ByteArrayInputStream(bytes);
    }

    @Override
    public <V> void writeVariable(Variable<V> var, V value) throws IOException {
        switch (var.getPersistenceType()) {
            case TEXT:
                writeAsText(var, value);
                return;
            case JSON:
                writeAsJson(var, value);
                return;
            case BYTES:
                writeAsInputStream(var, (InputStream)value);
                return;
            case NONE:
                values.put(var, value);
            default:
            // do nothing
        }
    }

    private void writeAsText(Variable var, Object o) {
        values.put(var, o.toString());
    }

    private void writeAsJson(Variable var, Object o) throws IOException {
        String json = JsonHandler.getInstance().objectToJson(o);
        values.put(var, json);
    }

    private void writeAsInputStream(Variable var, InputStream is) throws IOException {
        byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
        values.put(var, bytes);
    }

}
