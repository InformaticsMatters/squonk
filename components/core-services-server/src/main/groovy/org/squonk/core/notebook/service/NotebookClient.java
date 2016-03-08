package org.squonk.core.notebook.service;

import org.squonk.notebook.api2.NotebookDescriptor;
import org.squonk.notebook.api2.NotebookEditable;
import org.squonk.notebook.api2.NotebookSavepoint;

import java.io.InputStream;
import java.util.List;

/**
 * Interface for persisting notebook contents, versions and variables.
 *
 * Created by timbo on 08/03/16.
 */
public interface NotebookClient {

    public static final String DEFAULT_KEY = "default";

    /** Create a new notebook
     *
     * @param username
     * @param notebookName
     * @param notebookDescription
     * @return
     */
    NotebookDescriptor createNotebook(String username, String notebookName, String notebookDescription);

    /** All notebooks the user has access to
     *
     * @param username
     * @return
     */
    List<NotebookDescriptor> listNotebooks(String username);

    /** List all editables of this notebook that belong to this user
     *
     * @param notebookId
     * @param username
     * @return
     */
    List<NotebookEditable> listEditables(Long notebookId, String username);


    /** Create a new editable based on this parent.
     *
     * @param notebookId The ID of the notebook
     * @param parentId The ID of the parent snapshot
     * @param username The user
     */
    NotebookEditable createNotebookEditable(Long notebookId, Long parentId, String username);

    /** update the definition of this editable
     *
     * @param notebookId
     * @param editableId
     * @param json The contents of the notebook
     */
    NotebookEditable updateNotebookEditable(Long notebookId, Long editableId, String json);

    /** Create a new savepoint that is inserted in the history between this NotebookEditable and its parent.
     * This is done by creating a new editable whose parent is the current one, and then converting the current editable
     * to a savepoint. The savepoint cannot then be further modified (except for updating its description and label).
     * What is returned in the NEW editable, as that is what will be continued to be worked on. If your want the savepoint
     * then fetch it separately using the ID of the original editable.
     *
     * @param notebookId
     * @param editableId
     * @return The new Editable that is created.
     */
    public NotebookEditable createSavepoint(Long notebookId, Long editableId);

    /** Get all savepoints for this notebook.
     *
     * @param notebookId
     * @return
     */
    List<NotebookSavepoint> listSavepoints(Long notebookId);

    /** gives this savepoint a description that can be helpful to describe its purpose
     *
     * @param notebookId
     * @param savepointId
     * @param description
     */
    NotebookSavepoint setSavepointDescription(Long notebookId, Long savepointId, String description);

    /** Gives this savepoint a specific label. If a savepoint for this notebook with the same label already exists it MUST
     * first be cleared (set to null or another value) as duplicate labels are not permitted
     *
     * @param notebookId
     * @param savepointId
     * @param label the new label. If null then clears the label.
     */
    NotebookSavepoint setSavepointLabel(Long notebookId, Long savepointId, String label);


    /**
     *
     * @param sourceId
     * @param variableName
     * @return
     */
    default String readTextValue(Long sourceId, String variableName) {
        return readTextValue(sourceId, variableName, DEFAULT_KEY);
    }

    /**
     *
     * @param sourceId Can be a editable ID or a savepoint ID
     * @param variableName
     * @param key
     * @return
     */
    String readTextValue(Long sourceId, String variableName, String key);

    /** Read the variable with the default key for the notebook version with the specified label.
     *
     * @param notebookId
     * @param label
     * @param variableName
     * @return
     */
    default String readTextValueForLabel(Long notebookId, String label, String variableName) {
        return readTextValueForLabel(notebookId, label, variableName, DEFAULT_KEY);
    }

    /** Read the variable with the specified key for the notebook version with the specified label.
     *
     * @param notebookId
     * @param label
     * @param variableName
     * @param key
     * @return
     */
    String readTextValueForLabel(Long notebookId, String label, String variableName, String key);

    /** Save this variable using the default key name of 'default'.
     * Use this for single component variables.
     *
     * @param editableId
     * @param cellName
     * @param variableName
     * @param value
     */
    default void writeTextValue(Long editableId, String cellName, String variableName, String value) {
        writeTextValue(editableId, cellName, variableName, value, DEFAULT_KEY);
    }

    /** Save this variable using the specified key.
     * Use this for multi component variables where each part is saved under a different key name, but with the same variable name.
     * If the combination of editableId, variableName and key already exists this is an update operation, if not then it
     * inserts a new row.
     *
     * @param editableId
     * @param cellName
     * @param variableName
     * @param value
     * @param key
     */
    public void writeTextValue(Long editableId, String cellName, String variableName, String value, String key);

    /**
     *
     * @param sourceId
     * @param variableName
     * @return
     */
    default InputStream readStreamValue(Long sourceId, String variableName) {
        return readStreamValue(sourceId, variableName, DEFAULT_KEY);
    }

    /** Read a stream variable
     *
     * @param sourceId Can be a editable ID or a savepoint ID
     * @param variableName
     * @param key
     * @return An InputStream to the data. Ensure that this is closed when finished
     */
    InputStream readStreamValue(Long sourceId, String variableName, String key);

    /**
     *
     * @param notebookId
     * @param label
     * @param variableName
     * @return
     */
    default InputStream readStreamValueForLabel(Long notebookId, String label, String variableName) {
        return readStreamValueForLabel(notebookId, label, variableName, DEFAULT_KEY);
    }

    /**
     *
     * @param label
     * @param variableName
     * @param key
     * @return
     */
    InputStream readStreamValueForLabel(Long notebookId, String label, String variableName, String key);

    /**
     *
     * @param editableId
     * @param cellName
     * @param variableName
     * @param value
     */
    default void writeStreamValue(Long editableId, String cellName, String variableName, InputStream value) {
        writeStreamValue(editableId, cellName, variableName, value, DEFAULT_KEY);
    }


    /**
     *
     * @param editableId
     * @param variableName
     * @param key
     * @param value
     */
    void writeStreamValue(Long editableId, String cellName, String variableName, InputStream value, String key);
}
