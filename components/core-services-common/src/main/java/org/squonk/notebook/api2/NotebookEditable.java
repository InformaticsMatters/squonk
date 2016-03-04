package org.squonk.notebook.api2;

import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 29/02/16.
 */
public class NotebookEditable {

    private Long id;
    private Long notebookId;
    private Long parentId;
    private String owner;
    private Date createdDate;
    private Date lastUpdatedDate;
    private String content;

    public NotebookEditable() {

    }

    public NotebookEditable(Long id, Long notebookId, Long parentId, String owner, Date createdDate, Date lastUpdatedDate, String content) {
        this.id = id;
        this.notebookId = notebookId;
        this.parentId = parentId;
        this.owner = owner;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Long getNotebookId() {
        return notebookId;
    }

    public Long getParentId() {
        return parentId;
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

    public String getContent() {
        return content;
    }
}
