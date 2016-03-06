package org.squonk.notebook.api2;

import java.util.Date;
import java.util.List;

/**
 * Created by timbo on 29/02/16.
 */
public class NotebookEditable extends AbstractNotebookVersion {

    private String owner;


    public NotebookEditable() {

    }

    public NotebookEditable(Long id, Long notebookId, Long parentId, String owner, Date createdDate, Date lastUpdatedDate, String content) {
        super(id, notebookId, parentId, createdDate, lastUpdatedDate, content);
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

}
