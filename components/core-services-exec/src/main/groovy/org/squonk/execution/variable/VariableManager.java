package org.squonk.execution.variable;

import org.squonk.api.VariableHandler;
import org.squonk.api.VariableHandlerRegistry;
import org.squonk.client.VariableClient;
import org.squonk.execution.variable.impl.MemoryVariableClient;
import org.squonk.execution.variable.impl.VariableReadContext;
import org.squonk.execution.variable.impl.VariableWriteContext;
import org.squonk.notebook.api.VariableKey;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class VariableManager {

    public static Logger LOG = Logger.getLogger(VariableManager.class.getName());

    private static final VariableHandlerRegistry variableHandlerRegistry = VariableHandlerRegistry.INSTANCE;
    private final Map<VariableKey, Object> tmpValues = new HashMap<>();

    private final VariableClient client;
    private final Long notebookId;
    private final Long sourceId;

    public VariableManager(VariableClient client, Long notebookId,  Long sourceId) {
        this.client = client;
        this.notebookId = notebookId;
        this.sourceId = sourceId;
    }

    public <V> void putValue(VariableKey key, Class<V> type, V value) throws Exception {
        LOG.fine("putValue: " + key + " -> " +value);
        Long cellId = key.getCellId();
        String variableName = key.getVariableName();

        VariableHandler<V> vh = variableHandlerRegistry.lookup(type);
        if (vh != null) {
            VariableHandler.WriteContext context = new VariableWriteContext(client, notebookId, sourceId, cellId, variableName);
            vh.writeVariable(value, context);
        } else if (canBeHandledAsString(value.getClass())){
            client.writeTextValue(notebookId, sourceId, cellId, variableName, value.toString(), null);
        }
    }

    public <V> V getValue(VariableKey key, Class<V> type) throws Exception {
        LOG.fine("getValue " + key + " of type " + type);
        String variableName = key.getVariableName();
        VariableHandler<V> vh = variableHandlerRegistry.lookup(type);
        if (vh != null) {

            VariableHandler.ReadContext context = new VariableReadContext(client, notebookId, sourceId, variableName);
            V result = (V)vh.readVariable(context);
            return result;

        } else if (canBeHandledAsString(type)){
            Constructor c = type.getConstructor(String.class);
            String s = client.readTextValue(notebookId, sourceId, variableName, null);
            return (V)c.newInstance(s);
        }
        throw new IllegalArgumentException("Don't know how to handle value of type " + type.getName());
    }

    boolean canBeHandledAsString(Class cls) {
        for (Constructor c: cls.getConstructors()) {
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0].isAssignableFrom(String.class)) {
                return true;
            }
        }
        return false;
    }

}
