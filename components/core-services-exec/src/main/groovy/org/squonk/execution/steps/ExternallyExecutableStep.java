package org.squonk.execution.steps;

import org.apache.camel.TypeConverter;

import java.util.Map;

public interface ExternallyExecutableStep {

    /** Execute with the given inputs
     * This method is used when executing with externally provided data.
     *
     * @param inputs
     * @return
     * @throws Exception
     */
    Map<String,Object> doExecute(Map<String,Object> inputs, Map<String,Object> options, TypeConverter converter) throws Exception;
}
