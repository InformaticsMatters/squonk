package com.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *
 * @author timbo
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public class AbstractTransform {
    
}
