package org.squonk.client;

import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.api2.NotebookInstance;
import org.squonk.notebook.api2.NotebookSavepoint;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for persisting notebook contents, versions and variables.
 *
 * Created by timbo on 08/03/16.
 */
public interface NotebookClient {

    static final String DEFAULT_KEY = "default";

    enum VarType { t, s }

    /** Create a new notebook
     *
     * @param username
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    NotebookDescriptor createNotebook(String username, String name, String description) throws Exception;

    /** Update details of a notebook
     *
     * @param notebookId
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    NotebookDescriptor updateNotebook(Long notebookId, String name, String description) throws Exception;

    /** All notebooks the user has access to
     *
     * @param username
     * @return
     * @throws Exception
     */
    List<NotebookDescriptor> listNotebooks(String username) throws Exception;

    /** List all editables of this notebook that belong to this user
     *
     * @param notebookId
     * @param username
     * @return
     * @throws Exception
     */
    List<NotebookEditable> listEditables(Long notebookId, String username) throws Exception;


    /** Create a new editable based on this parent.
     *
     * @param notebookId The ID of the notebook
     * @param parentId The ID of the parent savepoint
     * @param username The user
     * @return
     * @throws Exception
     */
    NotebookEditable createEditable(Long notebookId, Long parentId, String username) throws Exception;

    /** update the definition of this editable
     *
     * @param notebookId
     * @param editableId
     * @param notebookInstance The definition of the notebook
     * @return
     * @throws Exception
     */
    NotebookEditable updateEditable(Long notebookId, Long editableId, NotebookInstance notebookInstance) throws Exception;

    /** Create a new savepoint that is inserted in the history between this NotebookEditable and its parent.
     * This is done by creating a new editable whose parent is the current one, and then converting the current editable
     * to a savepoint. The savepoint cannot then be further modified (except for updating its description and label).
     * What is returned in the NEW editable, as that is what will be continued to be worked on. If your want the savepoint
     * then fetch it separately using the ID of the original editable.
     *
     * @param notebookId
     * @param editableId
     * @return The new Editable that is created.
     * @throws Exception
     */
    NotebookEditable createSavepoint(Long notebookId, Long editableId) throws Exception;

    /** Get all savepoints for this notebook.
     *
     * @param notebookId
     * @return
     * @throws Exception
     */
    List<NotebookSavepoint> listSavepoints(Long notebookId) throws Exception;

    /** gives this savepoint a description that can be helpful to describe its purpose
     *
     * @param notebookId
     * @param savepointId
     * @param description
     * @return
     * @throws Exception
     */
    NotebookSavepoint setSavepointDescription(Long notebookId, Long savepointId, String description) throws Exception;

    /** Gives this savepoint a specific label. If a savepoint for this notebook with the same label already exists it MUST
     * first be cleared (set to null or another value) as duplicate labels are not permitted
     *
     * @param notebookId
     * @param savepointId
     * @param label the new label. If null then clears the label.
     * @return
     * @throws Exception
     */
    NotebookSavepoint setSavepointLabel(Long notebookId, Long savepointId, String label) throws Exception;


    /**
     *
     * @param notebookId
     * @param sourceId
     * @param variableName
     * @return
     * @throws Exception
     */
    default String readTextValue(Long notebookId, Long sourceId, String variableName) throws Exception {
        return readTextValue(notebookId, sourceId, variableName, DEFAULT_KEY);
    }

    /**
     *
     * @param notebookId
     * @param sourceId Can be a editable ID or a savepoint ID
     * @param variableName
     * @param key
     * @return
     * @throws Exception
     */
    String readTextValue(Long notebookId, Long sourceId, String variableName, String key) throws Exception;

    /** Read the variable with the default key for the notebook version with the specified label.
     *
     * @param notebookId
     * @param label
     * @param variableName
     * @return
     */
    default String readTextValue(Long notebookId, String label, String variableName) throws Exception {
        return readTextValue(notebookId, label, variableName, DEFAULT_KEY);
    }

    /** Read the variable with the specified key for the notebook version with the specified label.
     *
     * @param notebookId
     * @param label
     * @param variableName
     * @param key
     * @return
     * @throws Exception
     */
    String readTextValue(Long notebookId, String label, String variableName, String key) throws Exception;

    /** Save this variable using the default key name of 'default'.
     * Use this for single component variables.
     *
     * @param notebookId
     * @param editableId
     * @param cellId
     * @param variableName
     * @param value
     * @throws Exception
     */
    default void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value) throws Exception {
        writeTextValue(notebookId, editableId, cellId, variableName, value, DEFAULT_KEY);
    }

    /** Save this variable using the specified key.
     * Use this for multi component variables where each part is saved under a different key name, but with the same variable name.
     * If the combination of editableId, variableName and key already exists this is an update operation, if not then it
     * inserts a new row.
     *
     * @param notebookId
     * @param editableId
     * @param cellId
     * @param variableName
     * @param value
     * @param key
     * @throws Exception
     */
    void writeTextValue(Long notebookId, Long editableId, Long cellId, String variableName, String value, String key) throws Exception;

    /**
     * @param notebookId
     * @param sourceId
     * @param variableName
     * @return An InputStream to the data. Ensure that this is closed when finished
     * @throws Exception
     */
    default InputStream readStreamValue(Long notebookId, Long sourceId, String variableName) throws Exception {
        return readStreamValue(notebookId, sourceId, variableName, DEFAULT_KEY);
    }

    /** Read a stream variable
     *
     * @param notebookId
     * @param sourceId Can be a editable ID or a savepoint ID
     * @param variableName
     * @param key
     * @return An InputStream to the data. Ensure that this is closed when finished
     * @throws Exception
     */
    InputStream readStreamValue(Long notebookId, Long sourceId, String variableName, String key) throws Exception;

    /**
     *
     * @param notebookId
     * @param label
     * @param variableName
     * @return An InputStream to the data. Ensure that this is closed when finished
     * @throws Exception
     */
    default InputStream readStreamValue(Long notebookId, String label, String variableName) throws Exception {
        return readStreamValue(notebookId, label, variableName, DEFAULT_KEY);
    }

    /**
     * @param notebookId
     * @param label
     * @param variableName
     * @param key
     * @return An InputStream to the data. Ensure that this is closed when finished
     * @throws Exception
     */
    InputStream readStreamValue(Long notebookId, String label, String variableName, String key) throws Exception;

    /**
     * @param notebookId
     * @param editableId
     * @param cellId
     * @param variableName
     * @param value
     * @throws Exception
     */
    default void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value) throws Exception {
        writeStreamValue(notebookId, editableId, cellId, variableName, value, DEFAULT_KEY);
    }


    /**
     * @param
     * @param editableId
     * @param cellId
     * @param variableName
     * @param key
     * @param value
     * @throws Exception
     */
    void writeStreamValue(Long notebookId, Long editableId, Long cellId, String variableName, InputStream value, String key) throws Exception;
}
