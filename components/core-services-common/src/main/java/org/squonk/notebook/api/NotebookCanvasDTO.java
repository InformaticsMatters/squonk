/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private final Map<String, Object> properties = new LinkedHashMap<>();


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

        /**
         *
         */
        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
        private final Map<String,Object> options = new LinkedHashMap<>();

        @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
        private final Map<String,Object> settings = new LinkedHashMap<>();

        private final  List<BindingDTO> bindings = new ArrayList<>();
        private final  List<OptionBindingDTO> optionBindings = new ArrayList<>();
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

        public List<OptionBindingDTO> getOptionBindings() {
            return Collections.unmodifiableList(optionBindings);
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

        public CellDTO withOptionBinding(OptionBindingDTO binding) {
            optionBindings.add(binding);
            return this;
        }

        public void addOptionBinding(OptionBindingDTO binding) {
            optionBindings.add(binding);
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

        public Map<String, Object> getSettings() {
            return settings;
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


    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class OptionBindingDTO {

        private final String optionKey;
        private final Long producerId;
        private final String producerKey;

        public OptionBindingDTO(
                @JsonProperty("optionKey") String optionKey,
                @JsonProperty("producerId") Long producerId,
                @JsonProperty("producerKey") String producerKey) {
            this.optionKey = optionKey;
            this.producerId = producerId;
            this.producerKey = producerKey;
        }

        public String getOptionKey() {
            return optionKey;
        }

        public Long getProducerId() {
            return producerId;
        }

        public String getProducerKey() {
            return producerKey;
        }
    }

}
