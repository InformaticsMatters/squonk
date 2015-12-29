package org.squonk.execution.steps;

import org.squonk.execution.variable.PersistenceType;
import org.squonk.execution.variable.VariableManager;
import org.squonk.notebook.api.VariableKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public abstract class AbstractStep implements Step {

    private static final Logger LOG = Logger.getLogger(AbstractStep.class.getName());

    protected String outputProducerName;
    protected Map<String, Object> options;
    protected final Map<String, VariableKey> inputVariableMappings = new HashMap<>();
    protected final Map<String, String> outputVariableMappings = new HashMap<>();


    @Override
    public String getOutputProducerName() {
        return outputProducerName;
    }

    protected void dumpConfig(Level level) {
        if (LOG.isLoggable(level)) {
            StringBuilder b = new StringBuilder("Step configuration: [class:").append(this.getClass().getName())
                    .append(" producerName:").append(outputProducerName)
                    .append(" inputs:[");
            int count = 0;
            for (Map.Entry<String,VariableKey> e : inputVariableMappings.entrySet()) {
                if (count > 0) {
                    b.append(" ");
                }
                count++;
                b.append(e.getKey()).append(" -> ").append(e.getValue());
            }
            b.append("] outputs:[");
            count = 0;
            for (Map.Entry<String,String> e : outputVariableMappings.entrySet()) {
                if (count > 0) {
                    b.append(" ");
                }
                count++;
                b.append(e.getKey()).append(" -> ").append(e.getValue());
            }
            b.append("]");
            LOG.log(level, b.toString());
        }
    }

    @Override
    public void configure(String outputProducerName, Map<String, Object> options, Map<String, VariableKey> inputVariableMappings, Map<String, String> outputVariableMappings) {
        this.outputProducerName = outputProducerName;
        this.options = options;
        this.inputVariableMappings.putAll(inputVariableMappings);
        this.outputVariableMappings.putAll(outputVariableMappings);
    }

    protected VariableKey mapInputVariable(String name) {
        VariableKey mapped = inputVariableMappings.get(name);
        //if (mapped == null) {throw new IllegalArgumentException("Input variable mapping for " + name + " not present");}
        return mapped;
    }

    protected String mapOutputVariable(String name) {
        String mapped = outputVariableMappings.get(name);
        return (mapped == null) ? name : mapped;
    }

    protected <T> T fetchMappedInput(String internalName, Class<T> type, PersistenceType persistenceType, VariableManager varman) throws IOException {
        return fetchMappedInput(internalName, type, persistenceType, varman, false);
    }


    /**
     * Map the variable name using the variable mappings and fetch the
     * corresponding value.
     *
     * @param <T>
     * @param internalName
     * @param type
     * @param varman
     * @param required Whether a value is required
     * @return
     * @throws IOException
     * @throws IllegalStateException If required is true and no value is present
     */
    protected <T> T fetchMappedInput(String internalName, Class<T> type, PersistenceType persistenceType, VariableManager varman, boolean required) throws IOException {
        VariableKey mappedVar = mapInputVariable(internalName);
        if (mappedVar == null) {
            if (required) {
                throw new IllegalStateException("Mandatory input variable " + internalName + " not mapped to a notebook variable name");
            } else {
                return null;
            }
        }
        //System.out.println("Internal name: " + internalName);
        //System.out.println("Mapped name: " + mappedVarName);
        T input = fetchInput(mappedVar, type, persistenceType, varman);
        if (input == null && required) {
            throw new IllegalStateException("Mandatory input variable " + internalName + " does not have a value");
        }
        return input;
    }

    /**
     * Fetch the value with this name
     *
     * @param <T>
     * @param var
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchInput(VariableKey var, Class<T> type, PersistenceType persistenceType, VariableManager varman) throws IOException {
        //System.out.println("Getting value for variable " + externalName);
        T value = (T) varman.getValue(var, type, persistenceType);
        return value;
    }

    protected <T> T getOption(String name, Class<T> type) {
        if (options != null) {
            return (T) options.get(name);
        }
        return null;
    }

    protected <T> T getOption(String name, Class<T> type, T defaultValue) {
        T val = getOption(name, type);
        if (val == null) {
            return defaultValue;
        } else {
            return val;
        }
    }

    /**
     * Map the variable name and then create it. See {@link #createVariable} for 
     * details.
     *
     * @param <T>
     * @param localName
     * @param type
     * @param value
     * @param persistence
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> void createMappedOutput(String localName, Class<T> type, T value, PersistenceType persistence, VariableManager varman) throws IOException {
        String outFldName = mapOutputVariable(localName);
        createVariable(outFldName, type, value, persistence, varman);
    }

    /**
     * Creates a variable with the specified name. If the name starts with an
     * underscore (_) then a temporary variable (PersistenceType.NONE) is
     * created, otherwise the provided persistence type is used.
     *
     * @param <T>
     * @param mappedName
     * @param type
     * @param value
     * @param persistence
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> void createVariable(String mappedName, Class<T> type, T value, PersistenceType persistence, VariableManager varman) throws IOException {
        VariableKey key = new VariableKey(getOutputProducerName(), mappedName);
        varman.putValue(key, type, value, mappedName.startsWith("_") ? PersistenceType.NONE : persistence);
    }

}
