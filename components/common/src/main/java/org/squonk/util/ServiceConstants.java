/*
 * Copyright (c) 2019 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.util;

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
    String HEADER_AUTH = "Authorization";

    String KEY_SERVICE_REGISTRY = "KEY_SERVICE_REGISTRY";

}
