package org.squonk.core;

import org.squonk.util.IOUtils;
import org.squonk.util.StatsRecorder;

/**
 *
 * @author timbo
 */
public interface CommonConstants {
    
    public static final String HEADER_DATAITEM_NAME = "DataItemName";
    public static final String HEADER_SQUONK_USERNAME = "SquonkUsername";
    public static final String HEADER_JOB_ID = StatsRecorder.HEADER_SQUONK_JOB_ID;
    public static final String HEADER_JOB_SIZE = "SquonkJobSize";

    public static final String HEADER_JOB_PROCESSED_COUNT = "ProcessedCount";
    public static final String HEADER_JOB_ERROR_COUNT = "ErrorCount";
    public static final String HEADER_JOB_STATUS = "Status";

}
