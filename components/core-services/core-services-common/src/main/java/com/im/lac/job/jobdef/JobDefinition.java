package com.im.lac.job.jobdef;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author timbo
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface JobDefinition {
    
    enum ExecutionMode {
        ASYNC_SIMPLE,
        ASYNC_STREAMING,
        ASYNC_SPLIT_AND_QUEUE
    }
    
}
