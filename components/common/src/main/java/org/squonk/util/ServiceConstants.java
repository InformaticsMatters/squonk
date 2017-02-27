package org.squonk.util;

import org.squonk.util.StatsRecorder;

/**
 *
 * @author timbo
 */
public interface ServiceConstants {
    
    String HEADER_DATAITEM_NAME = "DataItemName";
    String HEADER_SQUONK_USERNAME = "SquonkUsername";
    String HEADER_JOB_ID = StatsRecorder.HEADER_SQUONK_JOB_ID;
    String HEADER_JOB_SIZE = "SquonkJobSize";
    String HEADER_METADATA = "Metadata";
    String HEADER_JOB_PROCESSED_COUNT = "ProcessedCount";
    String HEADER_JOB_ERROR_COUNT = "ErrorCount";
    String HEADER_JOB_STATUS = "Status";

    String KEY_SERVICE_REGISTRY = "KEY_SERVICE_REGISTRY";

}