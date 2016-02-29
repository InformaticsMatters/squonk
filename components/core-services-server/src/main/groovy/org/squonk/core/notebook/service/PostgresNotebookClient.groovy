package org.squonk.core.notebook.service

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.core.util.Utils
import org.squonk.notebook.api2.NotebookDescriptor
import org.squonk.notebook.api2.NotebookEditable
import org.squonk.notebook.api2.NotebookSavepoint

import javax.sql.DataSource

/**
 * Created by timbo on 29/02/16.
 */
@Log
class PostgresNotebookClient {

    public final static PostgresNotebookClient INSTANCE = new PostgresNotebookClient();

    private final Object lock = new Object();

    protected final DataSource dataSource = Utils.createDataSource()

    /** create a new notebook
     *
     * @param username
     * @return
     */
    public NotebookDescriptor createNotebook(String username, String notebookName, String notebookDescription) {
        Sql db = new Sql(dataSource.getConnection())
        try {
            db.withTransaction {
                def ids = db.executeInsert("INSERT INTO users.nb_descriptor (owner_id, name, description, created, updated) VALUES (?, ?, ? , NOW(), NOW())", [username, notebookName, notebookDescription])
                Long id = ids[0][0]
                log.info("Created Notebook of ID $id")
                return fetchNotebookDescriptor(db, id)
            }
        } finally {
            db.close()
        }
    }

    private NotebookDescriptor fetchNotebookDescriptor(Sql db, Long id) {
        def data = db.firstRow("SELECT * FROM users.nb_descriptor WHERE id = $id")
        if (data != null) {
            return buildNotebookDescriptor(data)
        } else {
            return null
        }
    }

    private NotebookDescriptor buildNotebookDescriptor(def data) {
        return new NotebookDescriptor(data.id, data.name, data.description, data.owner, data.createdDate, data.lastUpdatedDate)
    }

    /** all notebooks you have access to
     *
     * @param username
     * @return
     */
    public List<NotebookDescriptor> listNotebooks(String username) {
        return null;
    }

    //NotebookDescriptor fetchNotebookDefinition(Long definitionId);       // optional?

    /** typically the last one you had open
     *
     * @param definitionId
     * @param username
     * @return
     */
    public NotebookEditable getDefaultNotebookEditable(Long editableId, String username) {
        return null;
    }

    /** All the editables for the notebook
     *
     * @param notebookId
     * @return
     */
    public List<NotebookEditable> fetchEditables(Long notebookId) {

    }

    //NotebookEditable fetchNotebookEditable(Long editableId);             // optional?

    /** update the definition of this editable
     *
     * @param editable
     */
    public void storeNotebookEditable(NotebookEditable editable) {

    }

    /** create a new savepoint that is inserted in the history between this NotebookEditable and its parent
     *
     * @param editableId
     * @param username
     * @return
     */
    public NotebookSavepoint createSavepoint(Long editableId, String username) {

    }

    /**
     *
     * @param definitionId
     * @return
     */
    public List<NotebookSavepoint> fetchSavepoints(Long definitionId) {
        return null;
    }

    /** gives this savepoint a description that can be helpful to describe its purpose
     *
     * @param description
     */
    public NotebookSavepoint setSavepointDescription(Long savepointId, String description) {
        return null;
    }

    /** gives this savepoint a specific label, removing that label from a different savepoint if present.
     *
     * @param label the new label. If null then clears the label.
     */
    public NotebookSavepoint addSavepointLabel(Long savepointId, String label) {

    }

    public NotebookSavepoint deleteSavepointLabel(Long savepointId, String label) {

    }

    /** remove this savepoint, shortening the history as appropriate (and deleting variables associated ONLY with this savepoint)
     *
     * @param savepointId
     */
    public void deleteSavepoint(Long savepointId) {

    }


}
