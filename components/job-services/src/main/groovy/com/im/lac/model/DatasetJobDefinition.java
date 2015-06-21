package com.im.lac.model;

import com.im.lac.jobs.Job;

/**
 *
 * @author timbo
 */
public interface DatasetJobDefinition extends JobDefinition {

    Long getDatasetId();

    Job.DatasetMode getMode();
    
    String getDatasetName();

}
