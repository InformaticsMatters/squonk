package com.im.lac.services.job.variable;

import com.im.lac.types.MoleculeObject;
import com.squonk.types.io.JsonHandler;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 *
 * @author timbo
 */
public class CellExecutionClientVariableLoader implements VariableLoader {

    /**
     * Used for variable reading/writing
     */
    private CellExecutionClient client;

    /**
     * The cell name. If handled this way then a new loader needs to be created
     * for execution of each cell.
     */
    private String producerName;

    @Override
    public void save() throws IOException {
        // what does this do? Commit the transaction?
    }

    @Override
    public <V> V readFromText(String var, Class<V> type) throws IOException {
        String val = client.readTextValue(producerName, var);
        return convertFromText(val, type);
    }

    @Override
    public <V> V readFromJson(String var, Class<V> type) throws IOException {
        String json = client.readTextValue(producerName, var);
        return JsonHandler.getInstance().objectFromJson(json, type);
    }

    @Override
    public InputStream readFromBytes(String var) throws IOException {
        return client.readStreamValue(producerName, var);
    }

    @Override
    public void writeToText(String var, Object o) throws IOException {
        client.writeTextValue(producerName, var, o.toString());
    }

    @Override
    public void writeToJson(String var, Object o) throws IOException {
        String json = JsonHandler.getInstance().objectToJson(o);
        client.writeTextValue(producerName, var, json);
    }

    @Override
    public void writeToBytes(String var, InputStream is) throws IOException {
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

    /**
     * Mocks up the CellExecutionClient class
     */
    interface CellExecutionClient {

        //public CellDTO retrieveCell(String cellName);
        String readTextValue(String producerName, String variableName);

        Integer readIntegerValue(String producerName, String variableName);

        // this would be beter than the above 2 methods and would avoid the need for
        // methods to be added for every data type that needs to be handled 
        //<T> T readValueFromText(String producerName, String variableName, Class<T> type);
        //<T> void writeValueAsText(String producerName, String variableName, T value);
        InputStream readStreamValue(String producerName, String variableName);

        List<MoleculeObject> readFileValueAsMolecules(String producerName, String variableName);

        void writeTextValue(String producerName, String variableName, String value);

        void writeIntegerValue(String producerName, String variableName, Integer value);

        void writeStreamContents(String producerName, String variableName, InputStream inputStream);

        //public void writeStreamContents(String producerName, String variableName, StreamingOutput streamingOutput);
    }

}
