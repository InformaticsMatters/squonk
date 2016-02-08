package com.im.lac.job.jobdef;

import org.squonk.core.AccessMode;
import org.squonk.core.ServiceDescriptor;
import java.util.Map;

/**
 *
 * @author timbo
 */
public interface ServiceExecutionJobDefinition extends JobDefinition {

    String getServiceId();

    String getAccessModeId();

    void configureService(ServiceDescriptor serviceDescriptor, AccessMode accessMode, Map<String,Object> parameters);
    
    Map<String,Object> getParameters();

}
