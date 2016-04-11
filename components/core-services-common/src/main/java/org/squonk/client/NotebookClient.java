package org.squonk.client;

import org.squonk.notebook.api.*;

import java.util.List;

/**
 * Interface for persisting notebook contents and versions.
 *
 * Created by timbo on 08/03/16.
 */
public interface NotebookClient {

    /** Create a new notebook
     *
     * @param username
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    NotebookDTO createNotebook(String username, String name, String description) throws Exception;


    /** Delete the specified notebook
     *
     * @param notebookId
     * @throws Exception
     */
    boolean deleteNotebook(Long notebookId) throws Exception;

    /** Update details of a notebook
     *
     * @param notebookId
     * @param name
     * @param description
     * @return
     * @throws Exception
     */
    NotebookDTO updateNotebook(Long notebookId, String name, String description) throws Exception;

    /** All notebooks the user has access to
     *
     * @param username
     * @return
     * @throws Exception
     */
    List<NotebookDTO> listNotebooks(String username) throws Exception;

    /** List all editables of this notebook that belong to this user
     *
     * @param notebookId
     * @param username
     * @return
     * @throws Exception
     */
    List<NotebookEditableDTO> listEditables(Long notebookId, String username) throws Exception;


    /** Create a new editable based on this parent.
     *
     * @param notebookId The ID of the notebook
     * @param parentId The ID of the parent savepoint
     * @param username The user
     * @return
     * @throws Exception
     */
    NotebookEditableDTO createEditable(Long notebookId, Long parentId, String username) throws Exception;

    /** update the definition of this editable
     *
     * @param notebookId
     * @param editableId
     * @param canvasDTO The definition of the notebook
     * @return
     * @throws Exception
     */
    NotebookEditableDTO updateEditable(Long notebookId, Long editableId, NotebookCanvasDTO canvasDTO) throws Exception;

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
    NotebookEditableDTO createSavepoint(Long notebookId, Long editableId) throws Exception;

    /** Get all savepoints for this notebook.
     *
     * @param notebookId
     * @return
     * @throws Exception
     */
    List<NotebookSavepointDTO> listSavepoints(Long notebookId) throws Exception;

    /** gives this savepoint a description that can be helpful to describe its purpose
     *
     * @param notebookId
     * @param savepointId
     * @param description
     * @return
     * @throws Exception
     */
    NotebookSavepointDTO setSavepointDescription(Long notebookId, Long savepointId, String description) throws Exception;

    /** Gives this savepoint a specific label. If a savepoint for this notebook with the same label already exists it MUST
     * first be cleared (set to null or another value) as duplicate labels are not permitted
     *
     * @param notebookId
     * @param savepointId
     * @param label the new label. If null then clears the label.
     * @return
     * @throws Exception
     */
    NotebookSavepointDTO setSavepointLabel(Long notebookId, Long savepointId, String label) throws Exception;

}
