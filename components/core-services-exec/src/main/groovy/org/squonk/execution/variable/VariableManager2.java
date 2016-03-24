package org.squonk.execution.variable;

import org.squonk.api.VariableHandler;
import org.squonk.core.client.NotebookRestClient;
import org.squonk.execution.variable.impl.VariableReadContext;
import org.squonk.execution.variable.impl.VariableWriteContext;
import org.squonk.notebook.api.VariableKey;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class VariableManager2 {

    public static Logger LOG = Logger.getLogger(VariableManager2.class.getName());

    private final Map<VariableKey, Object> tmpValues = new HashMap<>();

    private final NotebookRestClient client;

    public VariableManager2(NotebookRestClient client) {
        this.client = client;
    }

    public <V> void putValue(VariableKey key, V value) throws IOException {
        Long notebookId = 0l; // TODO -get the right value
        Long editableId = 0l; // TODO -get the right value
        Long cellId = 0l;     // TODO -get the right value
        String variableName = key.getName();
        VariableHandler.WriteContext context = new VariableWriteContext(client, notebookId, editableId, cellId, variableName);
        if (canBeHandledAsVariable(value.getClass())) {
            VariableHandler v = (VariableHandler)value;
            v.writeVariable(value, context);
        } else if (canBeHandledAsString(value.getClass())){
            client.writeTextValue(notebookId, editableId, cellId, variableName, value.toString(), null);
        }
    }

    public <V> V getValue(VariableKey key, Class<V> type, PersistenceType persistenceType) throws Exception {
        LOG.info("Getting variable " + key.getName() + " from cell " + key.getProducerName() + " of type " + type + " using persistence type of " + persistenceType);
        Long notebookId = 0l; // TODO -get the right value
        Long sourceId = 0l;   // TODO -get the right value
        String variableName = key.getName();
        if (canBeHandledAsVariable(type)) {

            VariableHandler.ReadContext context = new VariableReadContext(client, notebookId, sourceId, variableName);

            for (Constructor c: type.getConstructors()) {
                if (c.getParameterCount() == 1 && c.getParameterTypes()[0].isAssignableFrom(VariableHandler.ReadContext.class)) {
                    return (V)c.newInstance(context);
                } else if (c.getParameterCount() == 2 && c.getParameterTypes()[0].isAssignableFrom(Class.class) && c.getParameterTypes()[0].isAssignableFrom(VariableHandler.ReadContext.class)) {
                    return (V)c.newInstance(type, context);
                }
            }
        } else if (canBeHandledAsString(type)){
            Constructor c = type.getConstructor(String.class);
            String s = client.readTextValue(notebookId, sourceId, variableName, null);
            return (V)c.newInstance(s);
        }
        throw new IllegalArgumentException("Don't know how to handle value of type " + type.getName());
    }

    boolean canBeHandledAsVariable(Class cls) {
        if (cls.isAssignableFrom(VariableHandler.class)) {
            for (Constructor c: cls.getConstructors()) {
                if (c.getParameterCount() == 1 && c.getParameterTypes()[0].isAssignableFrom(VariableHandler.ReadContext.class)) {
                    return true;
                } else if (c.getParameterCount() == 2 && c.getParameterTypes()[0].isAssignableFrom(Class.class) && c.getParameterTypes()[0].isAssignableFrom(VariableHandler.ReadContext.class)) {
                    return true;
                }
            }
        }
        return false;
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
