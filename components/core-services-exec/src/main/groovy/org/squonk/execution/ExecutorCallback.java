/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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
package org.squonk.execution;

import org.squonk.jobdef.JobStatus;

import java.io.IOException;

/** Call back interface for an executor to update its manager on its status
 *
 */
public interface ExecutorCallback {

    /** Update the status of the execution. A process should use this when the status changes, or for long running jobs,
     * at suitable periods to indicates that progress is being made.
     *
     * @param jobId The ID of the job to update
     * @param status The current status
     * @param event A message that describes the event that caused the update.
     * @param processedCount The number of records processed so far.
     * @param errorCount The number or errors encountered so far.
     * @return
     * @throws IOException
     */
    JobStatus updateStatus(String jobId, JobStatus.Status status, String event, Integer processedCount, Integer errorCount)
            throws IOException;

}
