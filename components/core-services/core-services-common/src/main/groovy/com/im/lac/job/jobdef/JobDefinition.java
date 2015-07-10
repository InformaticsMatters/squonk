package com.im.lac.job.jobdef;

/**
 *
 * @author timbo
 */
public interface JobDefinition {
    
    enum ExecutionMode {
        ASYNC_SIMPLE,
        ASYNC_STREAMING,
        ASYNC_SPLIT_AND_QUEUE
    }
    
}
