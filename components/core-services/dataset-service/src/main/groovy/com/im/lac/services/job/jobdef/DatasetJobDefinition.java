package com.im.lac.jobdef;

import com.im.lac.job.Job;
import com.im.lac.job.Job;

/**
 *
 * @author timbo
 */
public interface DatasetJobDefinition extends JobDefinition {

    Long getDatasetId();

    Job.DatasetMode getMode();
    
    String getDatasetName();

}
