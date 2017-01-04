package org.squonk.jobdef;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squonk.io.IODescriptor;

import java.io.Serializable;

/**
 *
 * @author timbo
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface JobDefinition extends Serializable {

    
}
