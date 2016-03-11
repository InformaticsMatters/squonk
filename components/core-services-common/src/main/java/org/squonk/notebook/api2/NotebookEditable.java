package org.squonk.notebook.api2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

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
            @JsonProperty("content") String content) {
        super(id, notebookId, parentId, createdDate, lastUpdatedDate, content);
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

}
