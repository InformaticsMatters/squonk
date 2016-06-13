package org.squonk.execution.steps;

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

    protected static final String MSG_PREPARING_INPUT = "Preparing input ...";
    protected static final String MSG_PREPARING_OUTPUT = "Writing output ...";
    protected static final String MSG_RECORDS_PROCESSED = "%s records processed";
    protected static final String MSG_PROCESSING_COMPLETE = "Processing complete";
    protected static final String MSG_PREPARING_CONTAINER = "Preparing Docker container";
    protected static final String MSG_RUNNING_CONTAINER = "Running Docker container";

    protected Long outputProducerId;
    protected String jobId;
    protected Map<String, Object> options;
    protected final Map<String, VariableKey> inputVariableMappings = new HashMap<>();
    protected final Map<String, String> outputVariableMappings = new HashMap<>();
    protected String statusMessage = null;


    @Override
    public Long getOutputProducerId() {
        return outputProducerId;
    }

    protected void dumpConfig(Level level) {
        if (LOG.isLoggable(level)) {
            StringBuilder b = new StringBuilder("Step configuration: [class:").append(this.getClass().getName())
                    .append(" producerID:").append(outputProducerId)
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
            b.append("] options:[");
            count = 0;
            for (Map.Entry<String,Object> e: options.entrySet()) {
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
    public void configure(Long outputProducerId, String jobId, Map<String, Object> options, Map<String, VariableKey> inputVariableMappings, Map<String, String> outputVariableMappings) {
        this.outputProducerId = outputProducerId;
        this.jobId = jobId;
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

    protected <T> T fetchMappedInput(String internalName, Class<T> type, VariableManager varman) throws Exception {
        return fetchMappedInput(internalName, type, varman, false);
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
    protected <T> T fetchMappedInput(String internalName, Class<T> type, VariableManager varman, boolean required) throws Exception {
        VariableKey mappedVar = mapInputVariable(internalName);
        LOG.fine("VariableKey mapped to " + internalName + " is " + mappedVar);
        if (mappedVar == null) {
            if (required) {
                throw new IllegalStateException("Mandatory input variable " + internalName + " not mapped to a notebook variable name");
            } else {
                return null;
            }
        }
        T input = fetchInput(mappedVar, type, varman);
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
    protected <T> T fetchInput(VariableKey var, Class<T> type, VariableManager varman) throws Exception {
        //System.out.println("Getting value for variable " + externalName);
        T value = (T) varman.getValue(var, type);
        return value;
    }

    protected Object getOption(String name) {
        return (options == null ? null : options.get(name));
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
     * Map the variable name and then submit it. See {@link #createVariable} for
     * details.
     *
     * @param <T>
     * @param localName
     * @param value
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> void createMappedOutput(String localName, Class<T> type, T value, VariableManager varman) throws Exception {
        String outFldName = mapOutputVariable(localName);
        createVariable(outFldName, type, value, varman);
    }

    /**
     * Creates a variable with the specified name. If the name starts with an
     * underscore (_) then a temporary variable (PersistenceType.NONE) is
     * created, otherwise the provided persistence type is used.
     *
     * @param <T>
     * @param mappedName
     * @param value
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> void createVariable(String mappedName, Class<T> type, T value, VariableManager varman) throws Exception {
        LOG.fine("Creating variable " + mappedName + "  for producer " + getOutputProducerId());
        VariableKey key = new VariableKey(getOutputProducerId(), mappedName);
        varman.putValue(key, type, value);
    }

    @Override
    public String getStatusMessage() {
        return statusMessage;
    }
}
