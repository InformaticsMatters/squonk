package com.im.lac.services.job.dao;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;

import java.util.List;

/**
 * Created by timbo on 01/01/16.
 */
public interface JobStatusDao {

    /** Creates the job
     *
     * @param jobdef The Job definition
     * @param totalCount The total number of work units, or null if unknown
     * @return The Job ID
     */
    String create(JobDefinition jobdef, Integer totalCount);

    /** Fetch the current status for the job with this ID
     *
     * @param id The Job ID
     * @return
     */
    JobStatus get(String id);

    /** Fetch jobs matching these query criteria
     *
     * @param query
     * @return
     */
    List<JobStatus> list(JobQuery query);

    /** Update the status of this job
     *
     * TODO - add ability to store error info to tell user what went wrong.
     *
     * @param id Job ID
     * @param status The new status
     * @param processedCount The number of work units processed, or null if unknown
     * @return The updated status
     */
    JobStatus update(String id, JobStatus.Status status, Integer processedCount);


}
