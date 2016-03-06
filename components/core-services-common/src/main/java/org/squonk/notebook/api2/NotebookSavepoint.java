package org.squonk.notebook.api2;

import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 29/02/16.
 */
public class NotebookSavepoint extends AbstractNotebookVersion {

    private String creator;
    private String description;
    private String label;

    public NotebookSavepoint() {

    }

    public NotebookSavepoint(Long id, Long notebookId, Long parentId, String creator, Date createdDate, Date updatedDate, String description, String label, String content) {
        super(id, notebookId, parentId, createdDate, updatedDate, content);
        this.creator = creator;
        this.description = description;
        this.label = label;
    }

    public String getCreator() {
        return creator;
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

}
