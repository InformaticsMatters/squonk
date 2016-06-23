package org.squonk.jobdef;

/**
 * Created by timbo on 31/12/15.
 */
public interface CellExecutorJobDefinition extends JobDefinition {

    Long getNotebookId();
    Long getEditableId();
    Long getCellId();

}
