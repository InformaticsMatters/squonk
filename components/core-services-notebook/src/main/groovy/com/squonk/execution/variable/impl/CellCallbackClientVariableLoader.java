package com.squonk.execution.variable.impl;

import com.squonk.execution.variable.*;
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

    /**
     * The cell name. If handled this way then a new loader needs to be created
     * for execution of each cell.
     */
    final private String producerName;

    public CellCallbackClientVariableLoader(CallbackClient client, String producerName) {
        this.client = client;
        this.producerName = producerName;
    }

    @Override
    public void save() throws IOException {
        // what does this need to do? Commit the transaction?
    }

    @Override
    public <V> V readFromText(String var, Class<V> type) throws IOException {
        String val = client.readTextValue(producerName, var);
        System.out.println("Read value: " + var + " -> " + val);
        return convertFromText(val, type);
    }

    @Override
    public <V> V readFromJson(String var, Class<V> type) throws IOException {
        String json = client.readTextValue(producerName, var);
        System.out.println("Read json: " + var + " -> " + json);
        return JsonHandler.getInstance().objectFromJson(json, type);
    }

    @Override
    public InputStream readBytes(String var, String label) throws IOException {
        System.out.println("Read bytes for : " + var);
        return client.readStreamValue(producerName, var);
    }

    @Override
    public void writeToText(String var, Object o) throws IOException {
        System.out.println("Writing value: " + var + " -> " + o);
        client.writeTextValue(producerName, var, o.toString());
    }

    @Override
    public void writeToJson(String var, Object o) throws IOException {
        System.out.println("Writing json: " + var + " -> " + o);
        String json = JsonHandler.getInstance().objectToJson(o);
        client.writeTextValue(producerName, var, json);
    }

    @Override
    public void writeToBytes(String var, String label, InputStream is) throws IOException {
        System.out.println("Writing bytes: " + var + " -> " + is);
        client.writeStreamContents(producerName, var, is);
    }

    @Override
    public void delete(String var) throws IOException {
        // do we need this or will this only be done through the notebook client
        throw new UnsupportedOperationException("Not supported yet.");
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
