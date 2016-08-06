package org.squonk.notebook.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.*;

/**
 * Created by timbo on 01/04/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookCanvasDTO {

    public static final Long LATEST_VERSION = 1L;
    private final Long version;
    private final Long lastCellId;
    private final List<CellDTO> cells = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();


    /** Create Canvas DTO of the current (latest) version

     * @param lastCellId
     */
    public NotebookCanvasDTO(Long lastCellId) {
        this(lastCellId,LATEST_VERSION, null);
    }

    public NotebookCanvasDTO(Long lastCellId, Map<String, Object> properties) {
        this(lastCellId,LATEST_VERSION, properties);
    }


    /** Constructor for creating Canvas DTO for an older version.
     * Client code probably never needs to use this.
     *
     * @param lastCellId
     * @param version
     */
    public NotebookCanvasDTO(
            @JsonProperty("lastCellId") Long lastCellId,
            @JsonProperty("version") Long version,
            @JsonProperty("properties") Map<String, Object> properties) {
        this.lastCellId = lastCellId;
        this.version = version;
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    public List<CellDTO> getCells() {
        return Collections.unmodifiableList(cells);
    }

    public Long getVersion() {
        return version;
    }

    public Long getLastCellId() {
        return lastCellId;
    }

    public NotebookCanvasDTO withCell(CellDTO cell) {
        cells.add(cell);
        return this;
    }

    /** Allows to store arbitary properties
     *
     * @param key
     * @param value
     * @return
     */
    public Object putProperty(String key, Object value) {
        return properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public <T> T getProperty(String key, Class<T> type) {
        return (T)properties.get(key);
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    public Map<String,Object> getProperties() {
        return properties;
    }

    public void addCell(CellDTO cell) {
        cells.add(cell);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class CellDTO {
        /** Cell ID */
        private final Long id;
        /** Cell version */
        private final Long version;
        /** the type of cell so that it can be created using the cell registry */
        private final String key;
        /** the display name of the cell (in cases where the user has renamed it) */
        private final String name;

        private final Integer top, left, width, height;

        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
        private final Map<String,Object> options = new LinkedHashMap<>();

        private final  List<BindingDTO> bindings = new ArrayList<>();
        // does the state need to be stored e.g. execution failed?

        public CellDTO(
                @JsonProperty("id") Long id,
                @JsonProperty("version") Long version,
                @JsonProperty("key") String key,
                @JsonProperty("name") String name,
                @JsonProperty("top") Integer top,
                @JsonProperty("left") Integer left,
                @JsonProperty("width") Integer width,
                @JsonProperty("height") Integer height) {
            this.id = id;
            this.version = version;
            this.key = key;
            this.name = name;
            this.top = top;
            this.left = left;
            this.width = width;
            this.height = height;
        }

        /** Constructor for cell with fixed size
         *
         * @param id
         * @param version
         * @param key
         * @param name
         * @param top
         * @param left
         */
        public CellDTO(Long id, Long version, String key, String name, Integer top, Integer left) {
            this(id, version, key, name, top, left, null, null);
        }

        public  Map<String,Object> getOptions() {
            return Collections.unmodifiableMap(options);
        }

        public List<BindingDTO> getBindings() {
            return Collections.unmodifiableList(bindings);
        }

        public CellDTO withOption(String key, Object value) {
            options.put(key, value);
            return this;
        }

        public void addOption(String key, Object value) {
            options.put(key, value);
        }

        public CellDTO withBinding(BindingDTO binding) {
            bindings.add(binding);
            return this;
        }

        public void addBinding(BindingDTO binding) {
            bindings.add(binding);
        }

        public Long getId() {
            return id;
        }

        public Long getVersion() {
            return version;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public Integer getTop() {
            return top;
        }

        public Integer getLeft() {
            return left;
        }

        public Integer getWidth() {
            return width;
        }

        public Integer getHeight() {
            return height;
        }
    }

    /** Defines the connections between cells.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class BindingDTO {
        /** The name of the cell input variable */
        private final String variableKey;
        /** The ID of the cell producing the variable */
        private final Long producerId;
        /** The name of the variable being outputted */
        private final String producerVariableName;

        public BindingDTO(
                @JsonProperty("variableKey") String variableKey,
                @JsonProperty("producerId") Long producerId,
                @JsonProperty("producerVariableName") String producerVariableName) {
            this.variableKey = variableKey;
            this.producerId = producerId;
            this.producerVariableName = producerVariableName;
        }

        public String getVariableKey() {
            return variableKey;
        }

        public Long getProducerId() {
            return producerId;
        }

        public String getProducerVariableName() {
            return producerVariableName;
        }
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
