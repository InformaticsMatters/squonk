/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core.service.notebook

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.client.NotebookVariableClient
import org.squonk.config.SquonkServerConfig
import org.squonk.core.util.Utils
import org.squonk.notebook.api.NotebookCanvasDTO
import org.squonk.notebook.api.NotebookDTO
import org.squonk.notebook.api.NotebookEditableDTO
import org.squonk.notebook.api.NotebookSavepointDTO
import org.squonk.types.io.JsonHandler

import javax.sql.DataSource
import java.sql.PreparedStatement
import java.sql.ResultSet

/** Notebook and Variable Client that persists data in a PostgreSQL database
 * Created by timbo on 29/02/16.
 */
@Log
class NotebookPostgresClient implements NotebookVariableClient {

    public final static NotebookPostgresClient INSTANCE = new NotebookPostgresClient()

    protected final DataSource dataSource = SquonkServerConfig.getSquonkDataSource();

    public NotebookPostgresClient() {
        dataSource = SquonkServerConfig.getSquonkDataSource()
    }

    public NotebookPostgresClient(DataSource dataSource) {
        this.dataSource = dataSource
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotebookDTO createNotebook(String username, String name, String description) {
        log.info("Creating notebook $name for user $username and $description description")
        Sql db = createSql()
        try {
            NotebookDTO result = null
            db.withTransaction {
                def keys = db.executeInsert(
                        "INSERT INTO users.nb_descriptor (owner_id, name, description, created, updated) " +
                                "(SELECT id, :name, :desc, NOW(), NOW() FROM users.users u WHERE u.username = :username)",
                        [username: username, name: name, desc: description])
                Long id = findInsertedId(keys)
                log.info("Created Notebook of ID $id for user $username")
                if (id == null) {
                    throw new IllegalStateException("Failed to insert notebook for user $username. Is username valid?")
                }

                // create the first editable so that we have something to work with
                // TODO - this can be made more efficient?
                Long userId = fetchIdForUsername(db, username)
                Long editableId = insertNotebookEditable(db, id, userId)
                log.info("Created editable $editableId for notebook $id");

                // fetch the nb definition
                result = fetchNotebookDescriptorById(db, id)
            }
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteNotebook(Long notebookId) {
        log.fine("Deleting notebook $notebookId")
        Sql db = createSql()
        boolean b = false;
        try {
            db.withTransaction {
                int rows = db.executeUpdate("DELETE FROM users.nb_descriptor WHERE id=$notebookId")
                if (rows == 1) {
                    b = true;
                } else {
                    log.warning("Failed to delete notebook $notebookId")
                }
            }
            return b;
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotebookDTO updateNotebook(Long notebookId, String name, String description) {
        log.fine("Updating notebook $notebookId with name $name and $description description")
        Sql db = createSql()
        try {
            NotebookDTO result = null
            db.withTransaction {
                int rows = db.executeUpdate(
                        "UPDATE users.nb_descriptor SET name=:name, description=:desc, updated=NOW() WHERE id=:notebookid",
                        [name: name, desc: description, notebookid:notebookId])
                if (rows != 1) {
                    throw new IllegalStateException("Failed to update notebook $notebookId")
                }
                // fetch the nb definition
                result = fetchNotebookDescriptorById(db, notebookId)
            }
            return result
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotebookDTO> listNotebooks(String username) {
        log.fine("Listing notebooks for user $username")
        Sql db = createSql()
        try {
            List<NotebookDTO> results = null
            db.withTransaction {
                results = fetchNotebookDescriptorsByUsername(db, username)
            }
            log.info("Found ${results.size()} notebooks for user $username")
            return results
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotebookDTO addNotebookToLayer(Long notebookId, String layer) throws Exception {

        log.info("Adding notebook $notebookId to layer $layer")
        // currently the layer name is ignored and assumed to be "public"
        return doNotebookLayerChange(notebookId, 1)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotebookDTO removeNotebookFromLayer(Long notebookId, String layer) throws Exception {
        log.info("Removing notebook $notebookId from layer $layer")
        // currently the layer name is ignored and assumed to be "public"
        return doNotebookLayerChange(notebookId, 0)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> listLayers(Long notebookId) throws Exception {

        Sql db = createSql()
        try {
            boolean isPublic = false
            db.withTransaction {
                isPublic = isNotebookPublic(db, notebookId)
            }
            List<String> l = new ArrayList<>()
            if (isPublic) {
                l.add("public")
            }
            return l;
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<NotebookEditableDTO> listEditables(Long notebookId, String username) {
        log.finer("Listing editables for notebook $notebookId and user $username")
        Sql db = createSql()
        try {
            List<NotebookEditableDTO> results = null
            db.withTransaction {
                results = fetchNotebookEditablesByUsername(db, notebookId, username)
            }
            log.fine("Found ${results.size()} editables for notebook $notebookId")
            return results
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotebookEditableDTO createEditable(Long notebookId, Long parentId, String username) {
        log.fine("Creating editable for notebook $notebookId with parent $parentId for $username")
        if (parentId == null) {
            throw new NullPointerException("Editable must have a parent")
        }
        Sql db = createSql()
        try {
            NotebookEditableDTO result = null
            db.withTransaction {
                Long userId = fetchIdForUsername(db, username)
                Long id = createNotebookEditableFromSavepoint(db, notebookId, parentId, userId)
                if (id == null) {
                    throw new IllegalStateException("Failed to create editable. Does the notebook $notebookId and parent $parentId exist?")
                }
                result = fetchNotebookEditableById(db, notebookId, id)
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
    @Override
    public NotebookEditableDTO updateEditable(Long notebookId, Long editableId, NotebookCanvasDTO canvasDTO) {
        String json = JsonHandler.getInstance().objectToJson(canvasDTO);
        Sql db = createSql()
        try {
            NotebookEditableDTO result = null
            db.withTransaction {
                int updates = db.executeUpdate("UPDATE users.nb_version SET nb_definition=${json}::jsonb, updated=NOW() WHERE id=$editableId AND notebook_id=$notebookId AND type='E'")
                if (updates != 1) {
                    throw new IllegalStateException("No update performed. Does the editable $editableId for notebook $notebookId exist?")
                }
                cleanCellData(db, notebookId, editableId, canvasDTO)
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
    @Override
    boolean deleteEditable(Long notebookId, Long editableId, String username) throws Exception {
        boolean b = false;
        Sql db = createSql()
        try {
            db.withTransaction {
                Long userId = fetchIdForUsername(db, username)
                int rows = 0
                if (userId != null) {
                    rows = db.executeUpdate("DELETE FROM users.nb_version WHERE notebook_id=$notebookId AND id=$editableId AND owner_id=$userId AND type='E'")
                }
                if (rows == 1) {
                    b = true;
                } else {
                    log.warning("Failed to delete editable $editableId")
                }
            }
            return b;
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotebookEditableDTO createSavepoint(Long notebookId, Long editableId, String description) {
        log.fine("Creating savepoint for editable $editableId")
        Sql db = createSql()
        try {
            NotebookEditableDTO result = null
            db.withTransaction {

                // convert the editable to a savepoint
                int updates = db.executeUpdate("UPDATE users.nb_version SET type='S', description=$description, created=NOW(), updated=NOW() WHERE id=$editableId AND notebook_id=$notebookId AND type='E'")
                if (updates != 1) {
                    throw new IllegalStateException("Failed to convert editable to savepoint. Does the editable $editableId for notebook $notebookId exist?")
                }

                // create the new editable
                def keys = db.executeInsert(
                        "INSERT INTO users.nb_version (notebook_id, parent_id, owner_id, type, created, updated, nb_definition) " +
                                "(SELECT notebook_id, id, owner_id, 'E', NOW(), NOW(), nb_definition FROM users.nb_version WHERE id=:editableId AND notebook_id=:notebookId)",
                        [notebookId: notebookId, editableId: editableId])

                Long id = findInsertedId(keys)
                log.info("Created new editable $id based on $editableId")
                if (id == null) {
                    throw new IllegalStateException("No insert performed. Does the editable $editableId for notebook $notebookId exist?")
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
    @Override
    public List<NotebookSavepointDTO> listSavepoints(Long notebookId) {
        Sql db = createSql()
        try {
            List<NotebookSavepointDTO> results = null
            db.withTransaction {
                results = fetchNotebookSavepoints(db, notebookId)
            }
            log.fine("Found ${results.size()} savepoints for notebook $notebookId")
            return results
        } finally {
            db.close()
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotebookSavepointDTO setSavepointDescription(Long notebookId, Long savepointId, String description) {
        Sql db = createSql()
        try {
            NotebookSavepointDTO result = null
            db.withTransaction {
                int updates = db.executeUpdate(
                        "UPDATE users.nb_version SET description=:description, updated=NOW() WHERE notebook_id=:notebookId AND id=:savepointId AND type='S' ",
                        [notebookId: notebookId, savepointId: savepointId, description: description])
                if (updates != 1) {
                    throw new IllegalStateException("Description not updated. Does the savepoint $savepointId for notebook $notebookId exist?")
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
    @Override
    public NotebookSavepointDTO setSavepointLabel(Long notebookId, Long savepointId, String label) {
        log.fine("Setting label for $notebookId:$savepointId to $label")
        Sql db = createSql()
        try {
            NotebookSavepointDTO result = null
            db.withTransaction {
                int updates = db.executeUpdate(
                        "UPDATE users.nb_version SET label=:label, updated=NOW() WHERE notebook_id=:notebookId AND id=:savepointId AND type ='S' ",
                        [notebookId: notebookId, savepointId: savepointId, label: label])
                if (updates != 1) {
                    throw new IllegalStateException("Label not updated. Does the savepoint $savepointId for notebook $notebookId exist?")
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
    @Override
    public String readTextValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) {
        log.fine("Reading text variable $variableName:$key for $sourceId")
        Sql db = createSql()
        try {
            String result = null
            db.withTransaction {
                result = doFetchVar(db, notebookId, sourceId, cellId, variableName, key, true)
            }
            return result
        } finally {
            db.close()
        }
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public String readTextValue(Long notebookId, String label, String variableName, String key) {
//        log.info("Reading text variable $variableName:$key for label $label")
//        return doReadValueForLabel(notebookId, label, variableName, key, true)
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value, String key) {
        // TODO - include the notebookId in the process to increase security
        log.fine("Writing text variable $variableName:$key for $editableId:$cellId")
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
    @Override
    public InputStream readStreamValue(Long notebookId, Long sourceId, Long cellId, String variableName, String key) {
        log.fine("Reading stream variable $variableName:$key for $sourceId")
        // not entirely clear why this works as the connection is closed immediately but the InputStream is still readable.
        Sql db = createSql()
        try {
            InputStream result = null
            db.withTransaction {
                log.fine("Fetching InputStream for $sourceId, $variableName, $key")
                result = doFetchVar(db, notebookId, sourceId, cellId, variableName, key, false)
            }
            return result
        } finally {
            db.close()
        }
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public InputStream readStreamValue(Long notebookId, String label, String variableName, String key) {
//        log.info("Reading stream variable $variableName:$key for label $label")
//        return doReadValueForLabel(notebookId, label, variableName, key, false)
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value, String key) {
        // TODO - include the notebookId in the process to increase security
        log.fine("Writing stream variable $variableName:$key for $editableId:$cellId")
        Sql db = new Sql(dataSource.getConnection()) {
            protected void setParameters(List<Object> params, PreparedStatement ps) {
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

    @Override
    void deleteVariable(Long notebookId, Long editableId, Long cellId, String variableName) throws Exception {
        Sql db = createSql()
        try {
            InputStream result = null
            db.withTransaction {
                log.info("Deleting variable $editableId, $cellId, $variableName")
                db.executeUpdate("DELETE FROM users.nb_variable WHERE source_id=$editableId AND cell_id=$cellId AND var_name=$variableName")
            }
        } finally {
            db.close()
        }
    }
// ------------------- private implementation methods -----------------------

    protected Sql createSql() {
        new Sql(dataSource.getConnection())
    }

    private Long findInsertedId(def keys) {
        if (keys != null && keys.size() == 1) {
            return keys[0][0]
        }
        return null
    }

    private static String SQL_NB_FETCH_PREFIX = '''\
        |SELECT u.username, s.* FROM users.users u
        |  JOIN (SELECT n.*,
        |    sum(case v.type when 'S' then 1 else 0 end) sp_count,
        |    sum(case v.type when 'E' then 1 else 0 end) ed_count,
        |    sum((case v.type when 'E' THEN 1 else 0 end) * (case v.owner_id when n.owner_id THEN 1 else 0 end)) my_count
        |  FROM users.nb_descriptor n
        |  JOIN users.nb_version v ON v.notebook_id = n.id'''.stripMargin()

    private static String SQL_NB_FETCH_BY_ID = SQL_NB_FETCH_PREFIX + '''
        |  WHERE n.id = :notebookId
        |  GROUP BY n.id) AS s
        |  ON s.owner_id = u.id'''.stripMargin()

    private static String SQL_NB_FETCH_BY_USERNAME = SQL_NB_FETCH_PREFIX + '''
        |  GROUP BY n.id) AS s
        |  ON s.owner_id = u.id
        |  WHERE s.visibility > 0 OR u.username = :username
        |  ORDER BY s.created DESC'''.stripMargin()

    private NotebookDTO fetchNotebookDescriptorById(Sql db, Long notebookId) {
        def data = db.firstRow(SQL_NB_FETCH_BY_ID, [notebookId: notebookId])
        return (data == null ? null : buildNotebookDescriptor(data))
    }

    private List<NotebookDTO> fetchNotebookDescriptorsByUsername(Sql db, String username) {
        log.fine("SQL: " + SQL_NB_FETCH_BY_USERNAME)
        List<NotebookDTO> results = new ArrayList<>()
        def data = db.eachRow(SQL_NB_FETCH_BY_USERNAME, [username: username]) {
            results << buildNotebookDescriptor(it)
        }
        return results
    }

    private NotebookDTO buildNotebookDescriptor(def data) {
        log.fine("Building notebook: $data")
        List<String> layers = []
        if (data.visibility > 0) {
            layers << 'public'
        }
        return new NotebookDTO(data.id, data.name, data.description, data.username, data.created, data.updated, layers,
                (int)data.sp_count, (int)data.my_count, (int)data.ed_count)
    }

    private NotebookDTO doNotebookLayerChange(Long notebookId, int value) throws Exception {

        // current implementation is private (0) or public (1)
        // this will be replaced by an implementation that uses layers
        Sql db = createSql()
        try {
            NotebookDTO result = null
            db.withTransaction {
                setVisibility(db, notebookId, value)
                result = fetchNotebookDescriptorById(db, notebookId)
            }
            return result
        } finally {
            db.close()
        }
    }


    /** this is a temp solution until layers are fully implemented
     *
     * @param db
     * @param notebookId
     * @return
     */
    private boolean isNotebookPublic(Sql db, Long notebookId) {
        def row = db.firstRow("SELECT visibility FROM users.nb_descriptor WHERE id=$notebookId")
        if (row != null) {
            return row[0] != 0
        }
        return false;
    }

    /** this is a temp solution until layers are fully implemented
     *
     * @param db
     * @param notebookId
     * @param visibility
     */
    private void setVisibility(Sql db, Long notebookId, int visibility) {
        int rows = db.executeUpdate("UPDATE users.nb_descriptor SET visibility=$visibility WHERE id=$notebookId")
    }

    /** Create the first editable that will be the initial one that's used when a new notebook is created
     *
     * @param db
     * @param notebookId
     * @param userId
     */
    private Long insertNotebookEditable(Sql db, Long notebookId, Long userId) {

        def keys = db.executeInsert("""INSERT INTO users.nb_version (notebook_id, owner_id, created, updated, type) VALUES
(:notebookId, :userId, NOW(), NOW(), 'E')"""
                , [notebookId: notebookId, userId: userId])
        Long id = findInsertedId(keys)
        log.fine("Created editable of ID $id")
        return id
    }

    /** Create an editable from an existing savepoint
     *
     * @param db
     * @param notebookId
     * @param parentId
     * @param userId
     * @return
     */
    private Long createNotebookEditableFromSavepoint(Sql db, Long notebookId, Long parentId, Long userId) {

        def keys = db.executeInsert("""\
            |INSERT INTO users.nb_version (notebook_id, parent_id, owner_id, created, updated, type, nb_definition)
            |  SELECT notebook_id, id, :userId, NOW(), NOW(), 'E', nb_definition
            |    FROM users.nb_version WHERE notebook_id=:notebookId AND id=:parentId AND type='S'""".stripMargin(),
                [notebookId: notebookId, parentId: parentId, userId: userId])
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


    private NotebookEditableDTO fetchNotebookEditableById(Sql db, Long notebookId, Long editableId) {
        def data = db.firstRow(SQL_ED_FETCH_BY_ID, [notebookId: notebookId, editableId: editableId])
        return (data == null ? null : buildNotebookEditable(data))
    }

    private List<NotebookEditableDTO> fetchNotebookEditablesByUsername(Sql db, Long notebookId, String username) {
        List<NotebookEditableDTO> results = new ArrayList<>()
        db.eachRow(SQL_ED_FETCH_BY_USERNAME, [notebookId: notebookId, username: username]) {
            results << buildNotebookEditable(it)
        }
        return results
    }

    private NotebookEditableDTO buildNotebookEditable(def data) {
        log.fine("Building editable: $data")
        String json = data.nb_definition
        NotebookCanvasDTO canvasDTO = (json == null ? null : JsonHandler.getInstance().objectFromJson(json, NotebookCanvasDTO.class))
        // Long id, Long notebookId, Long parentId, String owner, Date createdDate, Date lastUpdatedDate, String content
        return new NotebookEditableDTO(data.id, data.notebook_id, data.parent_id, data.username, data.created, data.updated, canvasDTO)
    }

    private static String SQL_SP_FETCH = """\
    |SELECT s.id, s.notebook_id, s.parent_id, s.created, s.updated, s.description, s.label, s.nb_definition::text, u.username
    |  FROM users.nb_version s
    |  JOIN users.users u ON u.id = s.owner_id
    |  WHERE s.notebook_id = :notebookId AND s.type = 'S'""".stripMargin()
    private static String SQL_SP_FETCH_BY_ID = SQL_SP_FETCH + " AND s.id = :savepointId"
    private static String SQL_SP_FETCH_ALL = SQL_SP_FETCH + "  ORDER BY s.updated DESC"

    private NotebookSavepointDTO fetchNotebookSavepointById(Sql db, Long notebookId, Long savepointId) {
        def data = db.firstRow(SQL_SP_FETCH_BY_ID, [notebookId: notebookId, savepointId: savepointId])
        return (data == null ? null : buildNotebookSavepoint(data))
    }

    private List<NotebookSavepointDTO> fetchNotebookSavepoints(Sql db, Long notebookId) {
        List<NotebookSavepointDTO> results = new ArrayList<>()
        db.eachRow(SQL_SP_FETCH_ALL, [notebookId: notebookId]) {
            results << buildNotebookSavepoint(it)
        }
        return results
    }

    private NotebookSavepointDTO buildNotebookSavepoint(def data) {
        log.finer("Building savepoint: $data")
        String json = data.nb_definition
        NotebookCanvasDTO canvasDTO = (json == null ? null : JsonHandler.getInstance().objectFromJson(json, NotebookCanvasDTO.class))
        //                           Long id,  Long notebookId  Long parentId, String owner, Date createdDate,Date updatedDate, String description, String label, String content
        return new NotebookSavepointDTO(data.id, data.notebook_id, data.parent_id, data.username, data.created, data.updated, data.description, data.label, canvasDTO)
    }

    private Object doFetchVar(Sql db, Long notebookId, Long sourceId, Long cellId, String variableName, String key, boolean isText) {

        // TODO - this can probably be optimised significantly

        log.fine("Looking for ${isText ? 'text' : 'stream'} variable $variableName:$key in source $sourceId, cell $cellId")

        def result = null
        boolean found = false

        String sql = """\
                |SELECT ${isText ? 'val_text' : 'val_blob'} FROM users.nb_variable v
                |  JOIN users.nb_version s ON s.id=v.source_id
                |  WHERE v.source_id=:source AND v.cell_id=:cell AND v.var_name=:varname AND v.var_key=:key AND s.notebook_id=:notebook""".stripMargin()
        log.fine("SQL: $sql")

        db.query(sql, [notebook:notebookId, source: sourceId, cell: cellId, varname: variableName, key: key ?: DEFAULT_KEY]) { ResultSet rs ->

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
            log.fine("Variable $variableName:$key not found in source $sourceId:$cellId")
            def row = db.firstRow("SELECT parent_id FROM users.nb_version WHERE id=? AND notebook_id=?", [sourceId, notebookId])
            if (row != null) {
                Long parent = row[0]
                if (parent == null) {
                    log.fine("No parent defined for source $sourceId, cell $cellId, so variable $variableName:$key does not exist")
                    return null
                } else {
                    log.fine("Looking for variable $variableName:$key in parent $parent")
                    return doFetchVar(db, notebookId, parent, cellId, variableName, key, isText)
                }
            } else {
                log.info("No row found for $sourceId - probably invalid source or notebook ID?")
                return null;
            }
        }
    }

//    private Object doReadValueForLabel(Long notebookId, String label, String variableName, String key, isText) {
//        Sql db = new Sql(squonkDataSource.getConnection())
//        try {
//            Object result = null
//            db.withTransaction {
//                Long sourceId = fetchSourceIdForLabel(db, notebookId, label)
//                if (sourceId == null) {
//                    log.info("Label $label not defined for notebook $notebookId")
//                } else {
//                    log.fine("Label $label resolved to source $sourceId")
//                    result = doFetchVar(db, notebookId, sourceId, variableName, key, isText)
//                }
//            }
//            return result
//        } finally {
//            db.close()
//        }
//    }
//
//    private Long fetchSourceIdForLabel(Sql db, Long notebookId, String label) {
//        def row = db.firstRow("SELECT id FROM users.nb_version WHERE notebook_id=$notebookId AND label=$label")
//        return (row ? row[0] : null)
//    }

    private void cleanCellData(Sql db, Long notebookId, Long editableId, NotebookCanvasDTO canvasDTO) {
        String sql = "DELETE FROM users.nb_variable WHERE source_id=?"
        if (canvasDTO.cells.size() > 0) {
            List cellIds = canvasDTO.cells*.id
            String s = cellIds.join(',')
            log.fine("Cleaning variable data for editable $editableId for cells other than $s")
            sql += " AND cell_id NOT IN (" + s + ")"
        }
        log.fine("SQL: $sql")
        int deletes = db.executeUpdate(sql, [editableId])
        if (deletes) {
            log.fine("Deleted stale data for $deletes variables")
        }
    }

}
