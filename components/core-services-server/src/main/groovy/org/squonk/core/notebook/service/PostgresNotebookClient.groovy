package org.squonk.core.notebook.service

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.core.util.Utils
import org.squonk.notebook.api2.NotebookDescriptor
import org.squonk.notebook.api2.NotebookEditable
import org.squonk.notebook.api2.NotebookSavepoint

import javax.sql.DataSource
import java.sql.PreparedStatement
import java.sql.ResultSet

/** NotebookClient that persists data in a PostgreSQL database
 * Created by timbo on 29/02/16.
 */
@Log
class PostgresNotebookClient implements NotebookClient {

    public final static PostgresNotebookClient INSTANCE = new PostgresNotebookClient();

    protected final DataSource dataSource = Utils.createDataSource()

    /**
     * {@inheritDoc}
     */
    public NotebookDescriptor createNotebook(String username, String notebookName, String notebookDescription) {
        Sql db = createSql()
        try {
            NotebookDescriptor result = null
            db.withTransaction {
                def keys = db.executeInsert(
                        "INSERT INTO users.nb_descriptor (owner_id, name, description, created, updated) " +
                                "(SELECT id, :name, :desc, NOW(), NOW() FROM users.users u WHERE u.username = :username)",
                        [username: username, name: notebookName, desc: notebookDescription])
                Long id = findInsertedId(keys)
                log.info("Created Notebook of ID $id")
                if (id == null) {
                    throw new IllegalStateException("Failed to insert notebook")
                }
                // fetch the nb definition
                result = fetchNotebookDescriptorById(db, id)
                // create the first editable so that we have something to work with
                // TODO - this can be made more efficient?
                Long userId = fetchIdForUsername(db, username)
                Long editableId = insertNotebookEditable(db, result.id, null, userId)
            }
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<NotebookDescriptor> listNotebooks(String username) {
        Sql db = createSql()
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

    /**
     * {@inheritDoc}
     */
    public List<NotebookEditable> listEditables(Long notebookId, String username) {
        Sql db = createSql()
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

    /**
     * {@inheritDoc}
     */
    public NotebookEditable createNotebookEditable(Long notebookId, Long parentId, String username) {
        log.fine("Creating editable for notebook $notebookId with parent $parentId for $username")
        Sql db = createSql()
        try {
            NotebookEditable result = null
            db.withTransaction {
                Long userId = fetchIdForUsername(db, username)
                result = insertNotebookEditable(db, notebookId, parentId, userId)
            }
            log.info("Created editable ${result?.id} for notebook $notebookId with parent $parentId for $username")
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public NotebookEditable updateNotebookEditable(Long notebookId, Long editableId, String json) {
        Sql db = createSql()
        try {
            NotebookEditable result = null
            db.withTransaction {
                int updates = db.executeUpdate("UPDATE users.nb_version SET nb_definition=${json}::jsonb, updated=NOW() WHERE id=$editableId AND notebook_id=$notebookId AND type='E'")
                if (updates != 1) {
                    throw new IllegalStateException("No update performed. Does the editable with these criteria exist: notebook id=$notebookId, editable id=$editableId")
                }
                result = fetchNotebookEditableById(db, notebookId, editableId)
            }
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public NotebookEditable createSavepoint(Long notebookId, Long editableId) {
        log.fine("Creating savepoint for editable $editableId")
        Sql db = createSql()
        try {
            NotebookEditable result = null
            db.withTransaction {
                // create the new editable
                def keys = db.executeInsert(
                        "INSERT INTO users.nb_version (notebook_id, parent_id, owner_id, type, created, updated, nb_definition) " +
                                "(SELECT notebook_id, id, owner_id, 'E', NOW(), NOW(), nb_definition FROM users.nb_version WHERE id = :editableId AND notebook_id = :notebookId AND type = 'E')",
                        [notebookId: notebookId, editableId: editableId])

                Long id = findInsertedId(keys)
                log.info("Created new editable $id based on $editableId")
                if (id == null) {
                    throw new IllegalStateException("No insert performed. Does the editable with these criteria exist: notebook id=$notebookId, editable id=$editableId")
                }
                // convert the old editable to a savepoint
                int updates = db.executeUpdate("UPDATE users.nb_version SET type='S', updated = NOW() WHERE id = $editableId AND notebook_id = $notebookId")
                if (updates != 1) {
                    throw new IllegalStateException("Failed to convert editable to savepoint. Does the editable with these criteria exist: notebook id=$notebookId, editable id=$editableId")
                }

                // fetch the newly created editable
                result = fetchNotebookEditableById(db, notebookId, id)

            }
            log.info("Created savepoint ${result?.id} for editable $editableId")
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<NotebookSavepoint> listSavepoints(Long notebookId) {
        Sql db = createSql()
        try {
            List<NotebookSavepoint> results = null
            db.withTransaction {
                results = fetchNotebookSavepoints(db, notebookId)
            }
            return results
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public NotebookSavepoint setSavepointDescription(Long notebookId, Long savepointId, String description) {
        Sql db = createSql()
        try {
            NotebookSavepoint result = null
            db.withTransaction {
                int updates = db.executeUpdate(
                        "UPDATE users.nb_version SET description=:description, updated=NOW() WHERE notebook_id=:notebookId AND id=:savepointId AND type='S' ",
                        [notebookId: notebookId, savepointId: savepointId, description: description])
                if (updates != 1) {
                    throw new IllegalStateException("Description not updated. Does the savepoint with these criteria exist: notebook id=$notebookId, editable id=$savepointId")
                }

                result = fetchNotebookSavepointById(db, notebookId, savepointId)
            }
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public NotebookSavepoint setSavepointLabel(Long notebookId, Long savepointId, String label) {
        log.fine("Setting label for $notebookId:$savepointId to $label")
        Sql db = createSql()
        try {
            NotebookSavepoint result = null
            db.withTransaction {
                int updates = db.executeUpdate(
                        "UPDATE users.nb_version SET label=:label, updated=NOW() WHERE notebook_id=:notebookId AND id=:savepointId AND type ='S' ",
                        [notebookId: notebookId, savepointId: savepointId, label: label])
                if (updates != 1) {
                    throw new IllegalStateException("Label not updated. Does the savepoint with these criteria exist: notebook id=$notebookId, editable id=$savepointId")
                }
                result = fetchNotebookSavepointById(db, notebookId, savepointId)
                log.info("Label for savepoint $notebookId:$savepointId set to ${result.label}")
            }
            return result
        } finally {
            db.close()
        }
    }

    // ------------------- variable handling methods -----------------------

//            id              SERIAL PRIMARY KEY,
//            source_id       INT NOT NULL,
//            cell_id         INT NOT NULL,
//            var_name        VARCHAR(50) NOT NULL,
//            var_key         VARCHAR(20) NOT NULL,
//            val_txt         TEXT,
//            val_blob        BYTEA,

    /**
     * {@inheritDoc}
     */
    public String readTextValue(Long sourceId, String variableName, String key) {
        log.info("Reading text variable $variableName:$key for $sourceId")
        Sql db = createSql()
        try {
            String result = null
            db.withTransaction {
                result = doFetchVar(db, sourceId, variableName, key, true)
            }
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public String readTextValueForLabel(Long notebookId, String label, String variableName, String key) {
        log.info("Reading text variable $variableName:$key for label $label")
        return doReadValueForLabel(notebookId, label, variableName, key, true)
    }

    /**
     * {@inheritDoc}
     */
    public void writeTextValue(Long editableId, Long cellId, String variableName, String value, String key) {
        log.info("Writing text variable $variableName:$key for $editableId")
        Sql db = createSql()
        try {

            db.executeInsert("""\
                |INSERT INTO users.nb_variable AS t (source_id, cell_id, var_name, var_key, created, updated, val_text)
                |  VALUES (:sourceId, :cellId, :variableName, :key, NOW(), NOW(), :value )
                |  ON CONFLICT ON CONSTRAINT nbvar_uq DO UPDATE
                |    SET val_text=EXCLUDED.val_text, updated=NOW()
                |      WHERE t.source_id=EXCLUDED.source_id AND t.cell_id=EXCLUDED.cell_id AND t.var_name=EXCLUDED.var_name AND t.var_key=EXCLUDED.var_key""".stripMargin(),
                    [sourceId: editableId, cellId: cellId, variableName: variableName, key: (key ?: DEFAULT_KEY), value: value])

        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream readStreamValue(Long sourceId, String variableName, String key) {
        log.info("Reading stream variable $variableName:$key for $sourceId")
        // not entirely clear why this works as the connection is closed immediately but the InputStream is still readable.
        Sql db = createSql()
        try {
            InputStream result = null
            db.withTransaction {
                log.info("Fetching InputStream for $sourceId, $variableName, $key")
                result = doFetchVar(db, sourceId, variableName, key, false)
            }
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream readStreamValueForLabel(Long notebookId, String label, String variableName, String key) {
        log.info("Reading stream variable $variableName:$key for label $label")
        return doReadValueForLabel(notebookId, label, variableName, key, false)
    }

    /**
     * {@inheritDoc}
     */
    public void writeStreamValue(Long editableId, Long cellId, String variableName, InputStream value, String key) {
        log.info("Writing stream variable $variableName:$key for $editableId")
        Sql db = new Sql(dataSource.getConnection()) {
            protected void setParameters(List<Object> params, PreparedStatement ps) {
                log.info("setParameters() ${params.size()}")
                ps.setLong(1, params[0])
                ps.setLong(2, params[1])
                ps.setString(3, params[2])
                ps.setString(4, params[3])
                ps.setBinaryStream(5, params[4])
            }
        }
        try {
            db.executeInsert("""\
                |INSERT INTO users.nb_variable AS t (source_id, cell_id, var_name, var_key, created, updated, val_blob)
                |  VALUES (?, ?, ?, ?, NOW(), NOW(), ?)
                |  ON CONFLICT ON CONSTRAINT nbvar_uq DO UPDATE
                |    SET val_blob=EXCLUDED.val_blob, updated=NOW()
                |      WHERE t.source_id=EXCLUDED.source_id AND t.cell_id=EXCLUDED.cell_id AND t.var_name=EXCLUDED.var_name AND t.var_key=EXCLUDED.var_key""".stripMargin(),
                    [editableId, cellId, variableName, key ?: DEFAULT_KEY, value])

        } finally {
            db.close()
        }
    }

    // ------------------- private implementation methods -----------------------

    private Sql createSql() {
        new Sql(dataSource.getConnection())
    }

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
        return (data == null ? null : buildNotebookDescriptor(data))
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
        log.fine("Created editable of ID $id")
        return id
    }

    private Long fetchIdForUsername(Sql db, String username) {
        return db.firstRow("SELECT id from users.users WHERE username = $username")[0]
    }

    private static String SQL_ED_FETCH = """\
    |SELECT v.id, v.notebook_id, v.parent_id, v.created, v.updated, v.nb_definition::text, u.username
    |  FROM users.nb_version v
    |  JOIN users.users u ON u.id = v.owner_id
    |  WHERE v.notebook_id = :notebookId AND v.type = 'E'""".stripMargin()
    private static String SQL_ED_FETCH_BY_ID = SQL_ED_FETCH + " AND v.id = :editableId"
    private
    static String SQL_ED_FETCH_BY_USERNAME = SQL_ED_FETCH + " AND u.username = :username\n  ORDER BY v.updated DESC"


    private NotebookEditable fetchNotebookEditableById(Sql db, Long notebookId, Long editableId) {
        def data = db.firstRow(SQL_ED_FETCH_BY_ID, [notebookId: notebookId, editableId: editableId])
        return (data == null ? null : buildNotebookEditable(data))
    }

    private List<NotebookDescriptor> fetchNotebookEditablesByUsername(Sql db, Long notebookId, String username) {
        List<NotebookDescriptor> results = new ArrayList<>()
        db.eachRow(SQL_ED_FETCH_BY_USERNAME, [notebookId: notebookId, username: username]) {
            results << buildNotebookEditable(it)
        }
        return results
    }

    private NotebookEditable buildNotebookEditable(def data) {
        log.fine("Building editable: $data")
        // Long id, Long notebookId, Long parentId, String owner, Date createdDate, Date lastUpdatedDate, String content
        return new NotebookEditable(data.id, data.notebook_id, data.parent_id, data.username, data.created, data.updated, data.nb_definition)
    }

    private static String SQL_SP_FETCH = """\
    |SELECT s.id, s.notebook_id, s.parent_id, s.created, s.updated, s.description, s.label, s.nb_definition::text, u.username
    |  FROM users.nb_version s
    |  JOIN users.users u ON u.id = s.owner_id
    |  WHERE s.notebook_id = :notebookId AND s.type = 'S'""".stripMargin()
    private static String SQL_SP_FETCH_BY_ID = SQL_SP_FETCH + " AND s.id = :savepointId"
    private static String SQL_SP_FETCH_ALL = SQL_SP_FETCH + "  ORDER BY s.updated DESC"

    private NotebookSavepoint fetchNotebookSavepointById(Sql db, Long notebookId, Long savepointId) {
        def data = db.firstRow(SQL_SP_FETCH_BY_ID, [notebookId: notebookId, savepointId: savepointId])
        return (data == null ? null : buildNotebookSavepoint(data))
    }

    private List<NotebookSavepoint> fetchNotebookSavepoints(Sql db, Long notebookId) {
        List<NotebookSavepoint> results = new ArrayList<>()
        db.eachRow(SQL_SP_FETCH_ALL, [notebookId: notebookId]) {
            results << buildNotebookSavepoint(it)
        }
        return results
    }

    private NotebookSavepoint buildNotebookSavepoint(def data) {
        log.finer("Building savepoint: $data")
        //                           Long id,  Long notebookId  Long parentId, String owner, Date createdDate,Date updatedDate, String description, String label, String content
        return new NotebookSavepoint(data.id, data.notebook_id, data.parent_id, data.username, data.created, data.updated, data.description, data.label, data.nb_definition)
    }

    private Object doFetchVar(Sql db, Long sourceId, String variableName, String key, boolean isText) {

        // TODO - this can probably be optimised significantly

        log.fine("Looking for ${isText ? 'text' : 'stream'} variable $variableName:$key in source $sourceId")

        def result = null
        boolean found = false

        String sql = """\
                |SELECT ${isText ? 'val_text' : 'val_blob'} FROM users.nb_variable
                |  WHERE source_id=:sourceId AND var_name=:variableName AND var_key=:key""".stripMargin()
        log.fine("SQL: $sql")

        db.query(sql, [sourceId: sourceId, variableName: variableName, key: key ?: DEFAULT_KEY]) { ResultSet rs ->

            if (rs.next()) {
                found = true
                if (isText) {
                    result = rs.getString(1)
                } else {
                    result = rs.getBinaryStream(1)
                }
            }
        }

        if (found) {
            return result
        } else {
            log.info("Variable $variableName:$key not found in source $sourceId")
            def row = db.firstRow("SELECT parent_id FROM users.nb_version WHERE id=?", [sourceId])
            if (row != null) {
                Long parent = row[0]
                if (parent == null) {
                    log.info("No parent defined for source $sourceId, so variable $variableName:$key does not exist")
                    return null
                } else {
                    log.info("Looking for variable $variableName:$key in parent $parent")
                    return doFetchVar(db, parent, variableName, key, isText)
                }
            } else {
                log.info("No row found for $sourceId - probably invalid ID?")
                return null;
            }
        }
    }

    private Object doReadValueForLabel(Long notebookId, String label, String variableName, String key, isText) {
        Sql db = new Sql(dataSource.getConnection())
        try {
            String result = null
            db.withTransaction {
                Long sourceId = fetchSourceIdForLabel(db, notebookId, label)
                if (sourceId == null) {
                    log.info("Label $label not defined for notebook $notebookId")
                } else {
                    log.fine("Label $label resolved to source $sourceId")
                    result = doFetchVar(db, sourceId, variableName, key, isText)
                }
            }
            return result
        } finally {
            db.close()
        }
    }

    private Long fetchSourceIdForLabel(Sql db, Long notebookId, String label) {
        def row = db.firstRow("SELECT id FROM users.nb_version WHERE notebook_id=$notebookId AND label=$label")
        return (row ? row[0] : null)
    }

}
