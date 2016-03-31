package org.squonk.notebook.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by timbo on 29/02/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookEditable extends AbstractNotebookVersion {

    private String owner;


    public NotebookEditable() {

    }

    public NotebookEditable(
            @JsonProperty("id") Long id,
            @JsonProperty("notebookId") Long notebookId,
            @JsonProperty("parentId") Long parentId,
            @JsonProperty("owner") String owner,
            @JsonProperty("createdDate") Date createdDate,
            @JsonProperty("lastUpdatedDate") Date lastUpdatedDate,
            @JsonProperty("notebookInstance") NotebookInstance notebookInstance) {
        super(id, notebookId, parentId, createdDate, lastUpdatedDate, notebookInstance);
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

}
