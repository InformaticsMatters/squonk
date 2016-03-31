package org.squonk.notebook.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by timbo on 29/02/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookDescriptor {

    private Long id;
    private String name;
    private String description;
    private String owner;
    private Date createdDate;
    private Date lastUpdatedDate;

    public NotebookDescriptor() {

    }

    public NotebookDescriptor(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("owner") String owner,
            @JsonProperty("createdDate") Date createdDate,
            @JsonProperty("lastUpdatedDate") Date lastUpdatedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
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
}
