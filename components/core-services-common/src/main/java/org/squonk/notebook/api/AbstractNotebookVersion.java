package org.squonk.notebook.api;

import java.util.Date;

/**
 * Created by timbo on 04/03/16.
 */
public abstract class AbstractNotebookVersion {


    private Long id;
    private Long notebookId;
    private Long parentId;
    private Date createdDate;
    private Date lastUpdatedDate;
    private NotebookInstance notebookInstance;

    AbstractNotebookVersion() {

    }

    AbstractNotebookVersion(Long id, Long notebookId, Long parentId, Date createdDate, Date lastUpdatedDate, NotebookInstance notebookInstance) {
        this.id = id;
        this.notebookId = notebookId;
        this.parentId = parentId;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.notebookInstance = notebookInstance;
        // TODO - work out how to handle this better
        if (notebookInstance != null) {
            notebookInstance.fixReferences();
        }
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public NotebookInstance getNotebookInstance() {
        return notebookInstance;
    }
}
