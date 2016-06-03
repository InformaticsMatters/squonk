package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by timbo on 17/05/16.
 */
public class FieldActionMapping {

    private final Map<String,String> mappings = new LinkedHashMap<>();


    public FieldActionMapping(@JsonProperty("mappings") Map<String,String> mappings) {
        this.mappings.putAll(mappings);
    }

    public FieldActionMapping() { }

    public void addMapping(String field, String action) {
        mappings.put(field, action);
    }

    public void removeMapping(String field) {
        mappings.remove(field);
    }

    @JsonIgnore
    public String getMapping(String field) {
        return mappings.get(field);
    }

    public Map<String,String> getMappings() {
        return mappings;
    }
}
