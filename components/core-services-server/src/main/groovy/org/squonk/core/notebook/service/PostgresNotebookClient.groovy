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
        NotebookDescriptor result = null
        try {
            db.withTransaction {
                def keys = db.executeInsert(
                        "INSERT INTO users.nb_descriptor (owner_id, name, description, created, updated) " +
                        "(SELECT id, :name, :desc, NOW(), NOW() FROM users.users u WHERE u.username = :username)",
                        [username: username, name: notebookName, desc: notebookDescription])
                Long id = findInsertedId(keys)
                log.info("Created Notebook of ID $id")
                if (id != null) {
                    // fetch the nb definition
                    result = fetchNotebookDescriptorById(db, id)
                    // create the first editable so that we have something to work with
                    // TODO - this can be made more efficient?
                    Long userId = fetchIdForUsername(db, username)
                    Long editableId = insertNotebookEditable(db, result.id, null, userId)
                }
            }
        } finally {
            db.close()
        }
        return result
    }

    /** all notebooks the user has access to
     *
     * @param username
     * @return
     */
    public List<NotebookDescriptor> listNotebooks(String username) {
        Sql db = new Sql(dataSource.getConnection())
        try {
            List<NotebookDescriptor> results = null
            db.withTransaction {
                results = fetchNotebookDescriptorsByUsername(db, username)
            }
            return results
        } finally {
            db.close()
        }
    }

    //NotebookDescriptor fetchNotebookDefinition(Long definitionId);       // optional?

    /** All all editables that belong to this user for this notebook
     *
     * @param notebookId
     * @return
     */
    public List<NotebookEditable> listEditables(Long notebookId, String username) {
        Sql db = new Sql(dataSource.getConnection())
        try {
            List<NotebookDescriptor> results = null
            db.withTransaction {
                results = fetchNotebookEditablesByUsername(db, notebookId, username)
            }
            return results
        } finally {
            db.close()
        }
    }

    //NotebookEditable fetchNotebookEditable(Long editableId);             // optional?

    /** Create a new editable based on this parent.
     *
     * @param notebookId The ID of the notebook
     * @param parentId The ID of the parent snapshot
     * @param username The user
     */
    public NotebookEditable createNotebookEditable(Long notebookId, Long parentId, String username) {
        Sql db = new Sql(dataSource.getConnection())
        try {
            NotebookEditable result = null
            db.withTransaction {
                Long userId = fetchIdForUsername(db, username)
                result = insertNotebookEditable(db, notebookId, parentId, userId)
            }
            return result
        } finally {
            db.close()
        }
    }

    /** update the definition of this editable
     *
     * @param editableId
     * @param json The contents of the notebook
     */
    public NotebookEditable updateNotebookEditable(Long notebookId, Long editableId, String json) {
        Sql db = new Sql(dataSource.getConnection())
        NotebookEditable result = null
        try {
            db.withTransaction {
                int udpdates = db.executeUpdate("UPDATE users.nb_version SET nb_definition = ${json}::jsonb, updated = NOW() WHERE id = $editableId AND notebook_id = $notebookId")
                result = fetchNotebookEditableById(db, notebookId, editableId)
            }
        } finally {
            db.close()
        }
        return result
    }

    /** create a new savepoint that is inserted in the history between this NotebookEditable and its parent
     *
     * @param editableId
     * @param username
     * @return
     */
    public NotebookSavepoint createSavepoint(Long editableId, String username) {
        return null
    }

    /**
     *
     * @param definitionId
     * @return
     */
    public List<NotebookSavepoint> listSavepoints(Long definitionId) {
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

    // ------------------- private implementation methods -----------------------

    private Long findInsertedId(def keys) {
        if (keys != null && keys.size() == 1) {
            return keys[0][0]
        }
        return null
    }


    private static String SQL_NB_FETCH = "SELECT n.*, u.username FROM users.nb_descriptor n JOIN users.users u ON u.id = n.owner_id"
    private static String SQL_NB_FETCH_BY_ID = SQL_NB_FETCH + " WHERE n.id = :notebookId"
    private static String SQL_NB_FETCH_BY_USERNAME = SQL_NB_FETCH + " WHERE u.username = :username ORDER BY n.created DESC"

    private NotebookDescriptor fetchNotebookDescriptorById(Sql db, Long notebookId) {
        def data = db.firstRow(SQL_NB_FETCH_BY_ID, [notebookId: notebookId])
        if (data != null) {
            return buildNotebookDescriptor(data)
        } else {
            return null
        }
    }

    private List<NotebookDescriptor> fetchNotebookDescriptorsByUsername(Sql db, String username) {
        List<NotebookDescriptor> results = new ArrayList<>()
        def data = db.eachRow(SQL_NB_FETCH_BY_USERNAME, [username: username]) {
            results << buildNotebookDescriptor(it)
        }
        return results
    }

    private NotebookDescriptor buildNotebookDescriptor(def data) {
        log.fine("Building notebook: $data")
        return new NotebookDescriptor(data.id, data.name, data.description, data.username, data.created, data.updated)
    }

    /** Create the first editable that will be the initial one that's used when a new notebook is created
     *
     * @param editable
     */
    private Long insertNotebookEditable(Sql db, Long notebookId, Long parentId, Long userId) {

        def keys = db.executeInsert("""INSERT INTO users.nb_version (notebook_id, parent_id, owner_id, created, updated, type) VALUES
(:notebookId, :parentId, :userId, NOW(), NOW(), 'E')"""
                , [notebookId: notebookId, userId: userId, parentId: parentId, userId: userId])
        Long id = findInsertedId(keys)
        log.info("Created editable of ID $id")
        return id
    }

    private Long fetchIdForUsername(Sql db, String username) {
        return db.firstRow("SELECT id from users.users WHERE username = $username")[0]
    }

    private
    static String SQL_ED_FETCH = "SELECT v.id, v.notebook_id, v.parent_id, v.created, v.updated, v.nb_definition::text, u.username FROM users.nb_version v JOIN users.users u ON u.id = v.owner_id"
    private
    static String SQL_ED_FETCH_BY_ID = SQL_ED_FETCH + " WHERE v.notebook_id = :notebookId AND v.type = 'E' AND v.id = :editableId"
    private
    static String SQL_ED_FETCH_BY_USERNAME = SQL_ED_FETCH + " WHERE v.notebook_id = :notebookId AND u.username = :username ORDER BY v.updated DESC"


    private NotebookEditable fetchNotebookEditableById(Sql db, Long notebookId, Long editableId) {
        def data = db.firstRow(SQL_ED_FETCH_BY_ID, [notebookId: notebookId, editableId: editableId])
        if (data != null) {
            return buildNotebookEditable(data)
        } else {
            return null
        }
    }

    private List<NotebookDescriptor> fetchNotebookEditablesByUsername(Sql db, Long notebookId, String username) {
        List<NotebookDescriptor> results = new ArrayList<>()
        def data = db.eachRow(SQL_ED_FETCH_BY_USERNAME, [notebookId: notebookId, username: username]) {
            results << buildNotebookEditable(it)
        }
        return results
    }

    private NotebookEditable buildNotebookEditable(def data) {
        log.fine("Building editable: $data")
        // Long id, Long parentId, String owner, Date createdDate, Date lastUpdatedDate
        return new NotebookEditable(data.id, data.parent_id, data.username, data.created, data.updated, data.nb_definition)
    }


}
