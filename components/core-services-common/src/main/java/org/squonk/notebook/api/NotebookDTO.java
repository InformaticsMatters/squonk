package org.squonk.notebook.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 29/02/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookDTO {

    private final Long id;
    private final String name;
    private final String description;
    private final String owner;
    private final Date createdDate;
    private final Date lastUpdatedDate;
    private final List<String> layers = new ArrayList<>();


    public NotebookDTO(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("owner") String owner,
            @JsonProperty("createdDate") Date createdDate,
            @JsonProperty("lastUpdatedDate") Date lastUpdatedDate,
            @JsonProperty("layers") List<String> layers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        if (layers != null && !layers.isEmpty()) {
            this.layers.addAll(layers);
        }
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public List<String> getLayers() {
        return layers;
    }
}
