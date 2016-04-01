package org.squonk.notebook.api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by timbo on 01/04/16.
 */
public class NotebookConfiguration implements Serializable {

    long lastCellId;
    List<CellConfiguration> cells;

    public class CellConfiguration {
        Long id;
        String key;  // the type of cell so that it can be created using the cell registry
        String name; // the display name of the cell (in cases where the user has renamed it)
        Integer top, left, width, height;
        List<OptionDefinition<?>> optionDefinitions;
        List<VariableDefinition> variableDefinitions;
        List<BindingDefinition> bindingDefinitions;
        // does the state need to be stored e.g. execution failed?
    }

    public class OptionDefinition<T> {
        String key;     // identifies which option (key property of the OptionDescriptor)
        T value;        // value, which must be writable as JSON
        List<T> values; // for things like multi selects, or do we handle everything as a List?
    }

    /** Defines the connections between cells.
     */
    public class BindingDefinition {
        String variableKey;         // The name of the cell input variable
        Long producerId;             // The ID of the cell producing the variable
        String producerVariableName; // The name of the variable being outputted
    }

    /** Definition of a variable that is output by a cell.
     * Unclear if this is strictly necessary, but it would be needed if we want strict control over writing variables.
     * If we had this we could have a variable_definition table that listed each defined variable so that only variable
     * values whose names were in this table could be written.
     * Might also be useful for variables that are dynamic in nature (presence depends on the state of the cell).
     *
     */
    public class VariableDefinition {
        String variableKey; // e.g. "output"
        Class primaryType;  // e.g. Dataset.class
        Class genericType;  // e.g. MoleculeObject.class or null for simple types
    }

}
