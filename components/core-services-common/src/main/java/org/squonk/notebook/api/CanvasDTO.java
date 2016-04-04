package org.squonk.notebook.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by timbo on 01/04/16.
 */
public class CanvasDTO implements Serializable {

    public final Long version;
    public final Long lastCellId;
    private final List<CellDTO> cells = new ArrayList<>();

    public CanvasDTO(Long version, Long lastCellId) {
        this.version = version;
        this.lastCellId = lastCellId;
    }

    public List<CellDTO> getCells() {
        return Collections.unmodifiableList(cells);
    }

    public class CellDTO {
        public final Long id;
        public final Long version;
        public final String key;  // the type of cell so that it can be created using the cell registry
        public final String name; // the display name of the cell (in cases where the user has renamed it)
        public final Integer top, left, width, height;
        private final List<OptionDTO<?>> options = new ArrayList<>();
        //List<VariableDTO> variables = new ArrayList<>();
        private final  List<BindingDTO> bindings = new ArrayList<>();
        // does the state need to be stored e.g. execution failed?

        public CellDTO(Long id,Long version,String key,String name, Integer top, Integer left, Integer width, Integer height) {
            this.id = id;
            this.version = version;
            this.key = key;
            this.name = name;
            this.top = top;
            this.left = left;
            this.width = width;
            this.height = height;
        }

        public  List<OptionDTO<?>> getOptions() {
            return Collections.unmodifiableList(options);
        }

        public List<BindingDTO> getBindings() {
            return Collections.unmodifiableList(bindings);
        }

        public CellDTO withOption(OptionDTO option) {
            options.add(option);
            return this;
        }

        public CellDTO withBinding(BindingDTO binding) {
            bindings.add(binding);
            return this;
        }

    }

    public class OptionDTO<T> {
        String key;     // identifies which option (key property of the OptionDescriptor)
        T value;        // value, which must be writable as JSON
        List<T> values; // for things like multi selects, or do we handle everything as a List?
    }

    /** Defines the connections between cells.
     */
    public class BindingDTO {
        String variableKey;         // The name of the cell input variable
        Long producerId;             // The ID of the cell producing the variable
        String producerVariableName; // The name of the variable being outputted
    }

//    /** Definition of a variable that is output by a cell.
//     * Unclear if this is strictly necessary, but it would be needed if we want strict control over writing variables.
//     * If we had this we could have a variable_definition table that listed each defined variable so that only variable
//     * values whose names were in this table could be written.
//     * Might also be useful for variables that are dynamic in nature (presence depends on the state of the cell).
//     *
//     */
//    public class VariableDTO {
//        String variableKey; // e.g. "output"
//        Class primaryType;  // e.g. Dataset.class
//        Class genericType;  // e.g. MoleculeObject.class or null for simple types
//    }

}
