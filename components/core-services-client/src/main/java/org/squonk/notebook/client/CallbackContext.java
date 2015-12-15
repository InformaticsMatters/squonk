package org.squonk.notebook.client;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class CallbackContext {
    private Long notebookId;

    public Long getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(Long notebookId) {
        this.notebookId = notebookId;
    }

}
