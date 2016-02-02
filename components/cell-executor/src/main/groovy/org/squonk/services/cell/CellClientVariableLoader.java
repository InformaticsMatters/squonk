package org.squonk.services.cell;

import org.squonk.execution.variable.VariableLoader;
import org.squonk.notebook.api.VariableKey;
import com.im.lac.cell.CellClient;
import org.squonk.types.io.JsonHandler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class CellClientVariableLoader implements VariableLoader {

    private static final Logger LOG = Logger.getLogger(CellClientVariableLoader.class.getName());

    /**
     * Used for variable reading/writing
     */
    @Inject
    private CellClient client;


    @Override
    public void save() throws IOException {
        // what does this need to do?
    }

    @Override
    public <V> V readFromText(VariableKey var, Class<V> type) throws IOException {
        LOG.finer("Reading text for: " + var);
        String val = client.readTextValue(var.getProducerName(), var.getName());
        LOG.fine("Read value: " + var + " -> " + val);
        return convertFromText(val, type);
    }

    @Override
    public <V> V readFromJson(VariableKey var, Class<V> type) throws IOException {
        LOG.finer("Reading json for: "  + var);
        String json = client.readTextValue(var.getProducerName(), var.getName());
        LOG.fine("Read json: " + var + " -> " + json);
        return JsonHandler.getInstance().objectFromJson(json, type);
    }

    @Override
    public InputStream readBytes(VariableKey var, String label) throws IOException {
        LOG.finer("Reading bytes for: " + var);
        InputStream is = client.readStreamValue(var.getProducerName(), var.getName());
        LOG.fine("Read bytes for : " + var + " -> " + is);
        return is;
    }

    @Override
    public void writeToText(VariableKey var, Object o) throws IOException {
        LOG.finer("Writing value: " + var + " -> " + o);
        client.writeTextValue(var.getProducerName(), var.getName(), o.toString());
    }

    @Override
    public void writeToJson(VariableKey var, Object o) throws IOException {
        LOG.finer("Writing json: " + var + " -> " + o);
        String json = JsonHandler.getInstance().objectToJson(o);
        client.writeTextValue(var.getProducerName(), var.getName(), json);
    }

    @Override
    public void writeToBytes(VariableKey var, String label, InputStream is) throws IOException {
        LOG.finer("Writing bytes: " + var + " -> " + is);
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
