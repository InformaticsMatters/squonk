package org.squonk.notebook.api2;

import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 29/02/16.
 */
public class NotebookSavepoint {

    private Long id;
    private Long parentId;
    private List<Long> editables;
    private String createdBy;
    private Date createdDate;
    private String description;
    private List<String> labels;

    public NotebookSavepoint() {

    }

    public NotebookSavepoint(Long id, Long parentId, String createdBy, Date createdDate, List<Long> editables, String description, List<String> labels) {
        this.id = id;
        this.parentId = parentId;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.editables = editables;
        this.description = description;
        this.labels = labels;
    }


    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }
    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Long> getEditables() {
        return editables;
    }
}
