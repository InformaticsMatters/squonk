package org.squonk.core;

import org.squonk.util.IOUtils;

/**
 *
 * @author timbo
 */
public interface CommonConstants {
    
    public static final String HEADER_DATAITEM_NAME = "DataItemName";
    public static final String HEADER_SQUONK_USERNAME = "SquonkUsername";
    public static final String HEADER_JOB_ID = "SquonkJobID";
    public static final String HEADER_JOB_SIZE = "SquonkJobSize";

    public static final String HEADER_JOB_PROCESSED_COUNT = "ProcessedCount";
    public static final String HEADER_JOB_ERROR_COUNT = "ErrorCount";
    public static final String HEADER_JOB_STATUS = "Status";


    public static final String HOST_CORE_SERVICES = IOUtils.getConfiguration("SQUONK_SERVICES_CORE", "http://demos.informaticsmatters.com:8091/coreservices/rest/v1");
    public static final String HOST_CORE_SERVICES_SERVICES = HOST_CORE_SERVICES + "/services";
    public static final String HOST_CHEM_SERVICES = "http://demos.informaticsmatters.com:8092";
    public static final String HOST_CDK_CALCULATORS = HOST_CHEM_SERVICES + "/chem-services-cdk-basic/rest/v1/calculators";

}