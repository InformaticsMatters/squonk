package com.im.lac.job.step;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/** Represents a step of a Job. A job is comprised of one of more steps.
 *
 * @author timbo
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class StepDefinition implements Serializable {

}
