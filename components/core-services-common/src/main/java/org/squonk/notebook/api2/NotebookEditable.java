package org.squonk.notebook.api2;

import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 29/02/16.
 */
public class NotebookEditable {

    private Long id;
    private Long parentId;
    private String owner;
    private Date createdDate;
    private Date lastUpdatedDate;

    public NotebookEditable() {

    }

    public NotebookEditable(Long id, Long parentId, String owner, Date createdDate, Date lastUpdatedDate) {
        this.id = id;
        this.parentId = parentId;
        this.owner = owner;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Long getId() {
        return id;
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

    public Object getInstance() {
        // TODO - comvert the return type to NotebookInstance and adapt
        return null;
    }
}
