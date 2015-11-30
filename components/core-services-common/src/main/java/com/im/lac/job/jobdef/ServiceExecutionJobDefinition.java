package com.im.lac.job.jobdef;

import com.im.lac.services.AccessMode;
import com.im.lac.services.ServiceDescriptor;
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
