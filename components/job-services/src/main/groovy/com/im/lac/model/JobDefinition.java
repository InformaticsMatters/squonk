package com.im.lac.model;

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
    
    ExecutionMode getExecutionMode();
    
    String getDestination();
    
}
