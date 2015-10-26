package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author timbo
 */
public abstract class AbstractStep implements Step {

    protected Map<String, Object> options;
    protected Map<String, String> variableMappings;

    @Override
    public void configure(Map<String, Object> options, Map<String, String> variableMappings) {
        this.options = options;
        this.variableMappings = variableMappings;
    }

    protected String mapVariableName(String name) {
        String mapped = null;
        if (variableMappings != null) {
            mapped = variableMappings.get(name);
        }
        return (mapped == null ? name : mapped);
    }

    /**
     * Map the variable name using the variable mappings and fetch the
     * corresponding value.
     *
     * @param <T>
     * @param unmappedVarName
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchMappedValue(String unmappedVarName, Class<T> type, VariableManager varman) throws IOException {
        String mappedVarName = mapVariableName(unmappedVarName);
        return fetchValue(mappedVarName, type, varman);
    }

    /**
     * Fetch the value with this name
     *
     * @param <T>
     * @param varName
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchValue(String varName, Class<T> type, VariableManager varman) throws IOException {
        Variable<T> var = varman.lookupVariable(varName);
        if (var == null) {
            throw new IllegalStateException("Required variable " + varName + " not present");
        }
        // TODO - use type convertor mechanism 
        T value = (T) varman.getValue(var);
        return value;
    }

    protected <T> T getOption(String name, Class<T> type) {
        if (options != null) {
            return (T) options.get(name);
        }
        return null;
    }

}
