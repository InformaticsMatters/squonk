package org.squonk.execution.variable.impl;

import org.squonk.execution.variable.VariableLoader;
import org.squonk.notebook.api.CellDTO;
import org.squonk.notebook.api.VariableKey;
import org.squonk.notebook.client.CallbackClient;
import com.squonk.types.io.JsonHandler;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author timbo
 */
public class CellCallbackClientVariableLoader implements VariableLoader {

    /**
     * Used for variable reading/writing
     */
    final private CallbackClient client;

    public CellCallbackClientVariableLoader(CallbackClient client) {
        this.client = client;
    }

    @Override
    public void save() throws IOException {
        // what does this need to do? Commit the transaction?
    }

    @Override
    public <V> V readFromText(VariableKey var, Class<V> type) throws IOException {
        System.out.println("Reading text for: " + var);
        String val = client.readTextValue(var.getProducerName(), var.getName());
        System.out.println("Read value: " + var + " -> " + val);
        return convertFromText(val, type);
    }

    @Override
    public <V> V readFromJson(VariableKey var, Class<V> type) throws IOException {
        System.out.println("Reading json for: "  + var);
        String json = client.readTextValue(var.getProducerName(), var.getName());
        System.out.println("Read json: " + var + " -> " + json);
        return JsonHandler.getInstance().objectFromJson(json, type);
    }

    @Override
    public InputStream readBytes(VariableKey var, String label) throws IOException {
        System.out.println("Reading bytes for: " + var);
        InputStream is = client.readStreamValue(var.getProducerName(), var.getName());
        System.out.println("Read bytes for : " + var + " -> " + is);
        return is;
    }

    @Override
    public void writeToText(VariableKey var, Object o) throws IOException {
        System.out.println("Writing value: " + var + " -> " + o);
        client.writeTextValue(var.getProducerName(), var.getName(), o.toString());
    }

    @Override
    public void writeToJson(VariableKey var, Object o) throws IOException {
        System.out.println("Writing json: " + var + " -> " + o);
        String json = JsonHandler.getInstance().objectToJson(o);
        client.writeTextValue(var.getProducerName(), var.getName(), json);
    }

    @Override
    public void writeToBytes(VariableKey var, String label, InputStream is) throws IOException {
        System.out.println("Writing bytes: " + var + " -> " + is);
        client.writeStreamContents(var.getProducerName(), var.getName(), is);
    }

    private <V> V convertFromText(String s, Class<V> type) throws IOException {
        // TODO - use TypeConvertor mechanism and fall back to reflection
        try {
            Constructor constr = type.getConstructor(String.class);
            return (V) constr.newInstance(s);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IOException("Failed to construct value", ex);
        }
    }

}
