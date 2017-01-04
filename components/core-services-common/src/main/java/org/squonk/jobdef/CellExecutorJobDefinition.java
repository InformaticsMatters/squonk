package org.squonk.jobdef;

import org.squonk.io.IODescriptor;

/**
 * Created by timbo on 31/12/15.
 */
public interface CellExecutorJobDefinition extends JobDefinition {

    Long getNotebookId();
    Long getEditableId();
    Long getCellId();

    /** The inputs the cell accepts.
     *
     * @return
     */
    IODescriptor[] getInputs();

    /** The outputs the cell produces.
     *
     * @return
     */
    IODescriptor[] getOutputs();
}
