package org.squonk.notebook.api2;

import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 29/02/16.
 */
public class NotebookSavepoint extends NotebookEditable {

    private String description;
    private String label;

    public NotebookSavepoint() {

    }

    public NotebookSavepoint(Long id, Long notebookId, Long parentId, String owner, Date createdDate, Date updatedDate, String description, String label, String content) {
        super(id, notebookId, parentId, owner, createdDate, updatedDate, content);
        this.description = description;
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

}
