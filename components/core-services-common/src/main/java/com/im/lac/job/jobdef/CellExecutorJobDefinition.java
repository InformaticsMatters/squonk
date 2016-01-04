package com.im.lac.job.jobdef;

/**
 * Created by timbo on 31/12/15.
 */
public interface CellExecutorJobDefinition extends JobDefinition {

    Long getNotebookId();
    String getCellName();

}
