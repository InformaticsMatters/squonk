package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class CellExecutionData implements Serializable {
    private final static long serialVersionUID = 1l;
    private Long notebookId;
    private Long editableId;
    private NotebookInstance notebookInstance;
    private Long cellId;

    public CellExecutionData() {}

    public CellExecutionData(Long notebookId, Long editableId, Long cellId, NotebookInstance notebookInstance) {
        this.notebookId = notebookId;
        this.editableId = editableId;
        this.cellId = cellId;
        this.notebookInstance = notebookInstance;
    }

    public Long getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(Long notebookId) {
        this.notebookId = notebookId;
    }

    public Long getEditableId() {
        return editableId;
    }

    public void setEditableId(Long editableId) {
        this.editableId = editableId;
    }

    public NotebookInstance getNotebookInstance() {
        return notebookInstance;
    }

    public void setNotebookInstance(NotebookInstance notebookInstance) {
        this.notebookInstance = notebookInstance;
    }

    public Long getCellId() {
        return cellId;
    }

    public void setCellId(Long cellId) {
        this.cellId = cellId;
    }
}
