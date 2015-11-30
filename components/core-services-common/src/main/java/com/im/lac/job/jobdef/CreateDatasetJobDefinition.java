package com.im.lac.job.jobdef;

/** Interface for a job that creates a new dataset e.g as a result of a database query 
 *
 * @author timbo
 */
public interface CreateDatasetJobDefinition extends JobDefinition {

    String getDatasetName();
    
    void configureDataset(String newDatasetName);

}
