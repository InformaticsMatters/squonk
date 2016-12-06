package org.squonk.execution.variable;

import org.squonk.api.VariableHandler;
import org.squonk.api.VariableHandlerRegistry;
import org.squonk.client.VariableClient;
import org.squonk.execution.variable.impl.VariableReadContext;
import org.squonk.execution.variable.impl.VariableWriteContext;
import org.squonk.notebook.api.VariableKey;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manager for variables that uses the VariableClient to persist values, and also allows temporary storage of variables.
 * Temp variables are distinguished by starting with an underscore character (_).
 *
 * If the VariableClient is null then all variables are stored as temp variables (this can be useful for testing)
 *
 * @author timbo
 */
public class VariableManager {

    public static Logger LOG = Logger.getLogger(VariableManager.class.getName());

    private static final VariableHandlerRegistry variableHandlerRegistry = VariableHandlerRegistry.INSTANCE;
    private final Map<String, byte[]> tmpValues = new ConcurrentHashMap<>();

    private final VariableClient client;
    private final Long notebookId;
    private final Long sourceId;

    public VariableManager(VariableClient client, Long notebookId, Long sourceId) {
        this.client = client;
        this.notebookId = notebookId;
        this.sourceId = sourceId;
    }

    public String getTmpVariableInfo() {
        StringBuilder b = new StringBuilder("Temp variables:\n");
        tmpValues.forEach((k,v) -> b.append("  ").append(k).append(" size ").append(v.length).append("\n"));
        return b.toString();
    }

    public <V> void putValue(VariableKey key, Class<V> type, V value) throws Exception {
        LOG.fine("putValue: " + key + " -> " + value);
        String variableName = key.getVariableName();
        VariableHandler<V> vh = variableHandlerRegistry.lookup(type);
        VariableHandler.WriteContext context = createWriteContext(key);
        if (vh != null) {
            vh.writeVariable(value, context);
        } else if (canBeHandledAsString(value.getClass())) {
            context.writeTextValue(value.toString());
        }
    }


    public <V> V getValue(VariableKey key, Class<V> type) throws Exception {
        LOG.fine("getValue " + key + " of type " + type);
        String variableName = key.getVariableName();
        VariableHandler<V> vh = variableHandlerRegistry.lookup(type);
        LOG.finer("Using variable handler " + vh + " for type " + type.getName());
        VariableHandler.ReadContext context = createReadContext(key);
        if (vh != null) {
            V result = (V) vh.readVariable(context);
            return result;

        } else if (canBeHandledAsString(type)) {
            Constructor c = type.getConstructor(String.class);
            String s = context.readTextValue();
            return s == null ? null : (V) c.newInstance(s);
        }
        throw new IllegalArgumentException("Don't know how to handle value of type " + type.getName());
    }

    boolean canBeHandledAsString(Class cls) {
        for (Constructor c : cls.getConstructors()) {
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0].isAssignableFrom(String.class)) {
                return true;
            }
        }
        return false;
    }

    private VariableHandler.WriteContext createWriteContext(VariableKey key) {
        if (client == null || key.getVariableName().startsWith("_")) {
            return new TmpContext(key.getCellId(), key.getVariableName());
        } else {
            return new VariableWriteContext(client, notebookId, sourceId, key.getCellId(), key.getVariableName());
        }
    }

    private VariableHandler.ReadContext createReadContext(VariableKey key) {
        if (client == null || key.getVariableName().startsWith("_")) {
            return new TmpContext(key.getCellId(), key.getVariableName());
        } else {
            return new VariableReadContext(client, notebookId, sourceId, key.getCellId(), key.getVariableName());
        }
    }


    class TmpContext implements VariableHandler.WriteContext, VariableHandler.ReadContext {

        Long cellId;
        String variableName;

        TmpContext(Long cellId, String variableName) {
            this.cellId = cellId;
            this.variableName = variableName;
        }

        String generateTextKey(String key) {
            return "T#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#" + key;
        }

        String generateStreamKey(String key) {
            return "S#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#" + key;
        }

        @Override
        public String readTextValue(String key) throws Exception {
            String storeKey = generateTextKey(key);
            LOG.fine("Reading tmp value " + storeKey);
            byte[] bytes = tmpValues.get(storeKey);
            return bytes == null ? null : new String(bytes);
        }

        @Override
        public InputStream readStreamValue(String key) throws Exception {
            String storeKey = generateStreamKey(key);
            LOG.fine("Reading tmp value " + storeKey);
            byte[] bytes = tmpValues.get(storeKey);
            return bytes == null ? null : new ByteArrayInputStream(bytes);
        }

        @Override
        public void writeTextValue(String value, String key) throws Exception {
            String storeKey = generateTextKey(key);
            LOG.fine("Writing tmp value " + storeKey);
            if (value == null) {
                tmpValues.remove(storeKey);
            } else {
                tmpValues.put(storeKey, value.getBytes());
            }
        }

        @Override
        public void writeStreamValue(InputStream value, String key) throws Exception {
            String storeKey = generateStreamKey(key);
            LOG.fine("Writing tmp value " + storeKey);
            if (value == null) {
                tmpValues.remove(storeKey);
            } else {
                tmpValues.put(storeKey, IOUtils.convertStreamToBytes(value));
            }
        }

        @Override
        public void deleteVariable() throws Exception {
            tmpValues.entrySet().removeIf(e -> e.getKey().startsWith("T#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#")
            || e.getKey().startsWith("S#" + notebookId + "#" + sourceId + "#" + cellId + "#" + variableName + "#"));
        }
    }

}
