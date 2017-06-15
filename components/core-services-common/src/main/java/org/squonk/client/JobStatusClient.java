/*
 * Copyright (c) 2017 Informatics Matters Ltd.
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

package org.squonk.client;

import org.squonk.jobdef.JobDefinition;
import org.squonk.jobdef.JobQuery;
import org.squonk.jobdef.JobStatus;

import java.io.IOException;
import java.util.List;

/**
 * Created by timbo on 04/01/16.
 */
public interface JobStatusClient {

    /** Creates the job
     *
     * @param jobdef The Job definition
     * @param totalCount The total number of work units, or null if unknown
     * @return The Job ID
     */
    JobStatus submit(JobDefinition jobdef, String username, Integer totalCount) throws IOException;

    /** Fetch the current status for the job with this ID
     *
     * @param id The Job ID
     * @return
     */
    JobStatus get(String id) throws IOException;

    /** Fetch jobs matching these query criteria
     *
     * @param query
     * @return
     */
    List<JobStatus> list(JobQuery query) throws IOException;

    /** Update the status of this job
     *
     * TODO - add ability to store error info to tell user what went wrong.
     *
     * @param id Job ID
     * @param status The new status, or null if no change
     * @param event A message that describes the update (can be null)
     * @param processedCount The number of work units processed, or null if unknown
     * @return The updated status
     */
    JobStatus updateStatus(String id, JobStatus.Status status, String event, Integer processedCount, Integer errorCount) throws IOException;

    default JobStatus updateStatus(String id, JobStatus.Status status, String event) throws IOException {
        return updateStatus(id, status, event, 0, 0);
    }

    default JobStatus updateStatus(String id, JobStatus.Status status) throws IOException {
        return updateStatus(id, status, null, 0, 0);
    }

    JobStatus incrementCounts(String id, int processsedCount, int errorCount) throws IOException;

}
