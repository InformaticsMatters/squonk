package com.squonk.execution.steps;

import com.squonk.execution.variable.Variable;
import com.squonk.execution.variable.VariableManager;
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
     * @param internalName
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchMappedValue(String internalName, Class<T> type, Variable.PersistenceType persistenceType, VariableManager varman) throws IOException {
        String mappedVarName = mapVariableName(internalName);
        //System.out.println("Internal name: " + internalName);
        //System.out.println("Mapped name: " + mappedVarName);
        return fetchValue(mappedVarName, type, persistenceType, varman);
    }

    /**
     * Fetch the value with this name
     *
     * @param <T>
     * @param externalName
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchValue(String externalName, Class<T> type, Variable.PersistenceType persistenceType, VariableManager varman) throws IOException {
        //System.out.println("Getting value for variable " + externalName);
        T value = (T) varman.getValue(externalName, type, persistenceType);
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
    protected <T> void createMappedVariable(String localName, Class<T> type, T value, Variable.PersistenceType persistence, VariableManager varman) throws IOException {
        String outFldName = mapVariableName(localName);
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
    protected <T> void createVariable(String mappedName, Class<T> type, T value, Variable.PersistenceType persistence, VariableManager varman) throws IOException {
        varman.putValue(mappedName, type, value, mappedName.startsWith("_") ? Variable.PersistenceType.NONE : persistence);
    }

}
