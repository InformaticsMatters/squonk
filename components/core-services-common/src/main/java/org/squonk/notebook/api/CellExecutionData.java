package org.squonk.notebook.api;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class CellExecutionData implements Serializable {
    private final static long serialVersionUID = 1l;
    private Long notebookId;
    private NotebookInstance notebookInstance;
    private Long cellId;

    public Long getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(Long notebookId) {
        this.notebookId = notebookId;
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
