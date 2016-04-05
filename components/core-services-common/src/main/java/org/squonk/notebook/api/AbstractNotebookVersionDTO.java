package org.squonk.notebook.api;

import java.util.Date;

/**
 * Created by timbo on 04/03/16.
 */
public abstract class AbstractNotebookVersionDTO {


    private Long id;
    private Long notebookId;
    private Long parentId;
    private Date createdDate;
    private Date lastUpdatedDate;
    private NotebookCanvasDTO canvasDTO;

    AbstractNotebookVersionDTO() {

    }

    AbstractNotebookVersionDTO(Long id, Long notebookId, Long parentId, Date createdDate, Date lastUpdatedDate, NotebookCanvasDTO canvasDTO) {
        this.id = id;
        this.notebookId = notebookId;
        this.parentId = parentId;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.canvasDTO = canvasDTO;
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

    public NotebookCanvasDTO getCanvasDTO() {
        return canvasDTO;
    }
}
