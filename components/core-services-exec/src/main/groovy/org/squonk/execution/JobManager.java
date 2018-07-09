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

import org.squonk.core.ServiceDescriptor;
import org.squonk.core.client.JobStatusRestClient;
import org.squonk.jobdef.ExternalJobDefinition;
import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.JobStatus.Status;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class that manages execution of jobs.
 * Currently this is a prototype for a new type of execution.
 * It will initially executor 'external' jobs (that is jobs that provide their own input data and are not related to any
 * notebook) but in time the notebook executors (the Step executors) will be refactored to use this new approach.
 *
 * This manager holds the state of executing jobs. A client will submit a job and then poll the manager until results are
 * ready, then fetch the results and finally call a cleanup method to remove the results and any execution artifacts.
 *
 */
public class JobManager implements ExecutorCallback {

    private static final Logger LOG = Logger.getLogger(JobManager.class.getName());

    @Inject
    protected JobStatusRestClient jobstatusClient;


    private final Map<String, ExecutionData> executionDataMap = new LinkedHashMap<>();

    /** Submit a new job. The call will return immediately with the JobStatus from which you can obtain the job's ID.
     * YOu then use that ID to check the status, and when the status changes to @{link JobStatus.Status.RESULTS_READY}
     * you can then fetch the results and then cleanup.
     *
     * @param serviceDescriptor
     * @param options
     * @param inputs
     * @param username
     * @return
     * @throws Exception
     */
    public JobStatus executeAsync(
            ServiceDescriptor serviceDescriptor,
            Map<String, Object> options,
            Map<String, Object> inputs,
            String username) throws Exception {

        return execute(serviceDescriptor, options, inputs, username, true);
    }


    /** Submit a job and wait for it to complete.
     * Only use if you know the job is very fast or for testing.
     *
     * @param serviceDescriptor
     * @param options
     * @param inputs
     * @param username
     * @return
     * @throws Exception
     */
    public JobStatus executeSync(
            ServiceDescriptor serviceDescriptor,
            Map<String, Object> options,
            Map<String, Object> inputs,
            String username) throws Exception {

        return execute(serviceDescriptor, options, inputs, username, false);
    }


    private JobStatus execute(
            ServiceDescriptor serviceDescriptor,
            Map<String, Object> options,
            Map<String, Object> inputs,
            String username,
            boolean async) throws Exception {

        ExternalJobDefinition jobDefinition = new ExternalJobDefinition(serviceDescriptor, options);
        ExternalExecutor executor = new ExternalExecutor(jobDefinition, this);
        ExecutionData executionData = new ExecutionData();
        executionData.executor = executor;
        executionData.jobStatus = JobStatus.create(executor.getJobId(), jobDefinition, username, new Date(), 0);
        executionDataMap.put(executor.getJobId(), executionData);

        for (Map.Entry<String, Object> e : inputs.entrySet()) {
            executor.addData(e.getKey(), e.getValue());
        }

        if (async) {
            // TODO - handle with a thread pool or work queue?
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        executor.execute();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to submit job", e);
                    }
                }
            };
            t.start();
        } else {
            executor.execute();
        }
        JobStatus jobStatus = updateStatus(executor.getJobId(), Status.RUNNING);
        return jobStatus;
    }

    /** Get the current execution status of a job you submitted.
     *
     * @param jobId
     * @return
     */
    public JobStatus getJobStatus(String jobId) {
        ExecutionData executionData = executionDataMap.get(jobId);
        return executionData == null ? null : executionData.jobStatus;
    }

    /** Get the results for a job. Only call this once the status is @{link JobStatus.Status.RESULTS_READY}.
     * Once you safely have the results make sure you call {@link #cleanupJob(String)}
     *
     * @param jobId
     * @return
     */
    public Map<String,Object> getJobResults(String jobId) {
        ExternalExecutor executor = findExecutor(jobId);
        return executor == null ? null : executor.getResults();
    }

    /** Must be called after the results have been fetched so that any execution artifacts (containers etc.) and data
     * files can be deleted.
     *
     * @param jobId
     */
    public void cleanupJob(String jobId) {
        ExternalExecutor executor = findExecutor(jobId);
        if (executor != null) {
            executor.cleanup();
            try {
                // set the persisted status to complete
                updateStatus(jobId, JobStatus.Status.COMPLETED);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to update JobStatus", e);
                // should we retry later?
            } finally {
                // job complete and state persisted so we can remove it
                executionDataMap.remove(jobId);
            }
        }
    }

    /** Expected to be called by a daemon process that cleans up jobs that a client seems to have forgotten about.
     *
     * @param timeSinceStarted
     * @param timeSinceResultsReady
     */
    public void purgeJobs(long timeSinceStarted, long timeSinceResultsReady) {
        long now = new Date().getTime();
        for (ExecutionData executionData: executionDataMap.values()) {
            JobStatus jobStatus = executionData.jobStatus;
            ExternalExecutor executor = executionData.executor;

            if (jobStatus.getStatus() == Status.RESULTS_READY &&
                    now - jobStatus.getCompleted().getTime() > timeSinceResultsReady) {
                // job completed but the results were never fetched

                // try to cleanup the runner
                try {
                    executor.cleanup();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to cleanup job " + jobStatus.getJobId());
                }

                // set status to error
                try {
                    updateStatus(jobStatus.getJobId(), Status.ERROR, ExecutableService.MSG_RESULTS_NOT_FETCHED, null, null);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to set job status to ERROR for job " + jobStatus.getJobId());
                }

                // purge - results can no longer be fetched
                executionDataMap.remove(jobStatus.getJobId());

            } else if (now - jobStatus.getStarted().getTime() > timeSinceStarted) {
                // job has been running for too long so we kill it

                // try to terminate the execution
                try {
                    executor.terminate();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to terminate job " + jobStatus.getJobId());
                }

                // set the status to error
                try {
                    updateStatus(jobStatus.getJobId(), Status.ERROR, ExecutableService.MSG_JOB_TOOK_TOO_LONG, null, null);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to set job status to ERROR for job " + jobStatus.getJobId());
                }

                // purge
                executionDataMap.remove(jobStatus.getJobId());

            }
        }
    }

    private ExternalExecutor findExecutor(String jobId) {
        ExecutionData executionData = executionDataMap.get(jobId);
        if (executionData == null) {
            LOG.warning("Job ID " + jobId + " not found. Either it is incorrect or has completed");
            return null;
        }
        ExternalExecutor executor = executionData.executor;
        if (executor == null) {
            throw new IllegalStateException("No executor. This is most unexpected");
        }
        return executor;
    }

    private JobStatus updateStatus(String jobId, Status status)
            throws IOException {
        return updateStatus(jobId, status, null, null, null);

    }

    @Override
    public JobStatus updateStatus(String jobId, Status status, String event, Integer processedCount, Integer errorCount)
            throws IOException {
        ExecutionData executionData = executionDataMap.get(jobId);
        if (executionData == null) {
            LOG.warning("Job ID " + jobId + " not found. Either it is incorrect or the job has completed");
            return null;
        }
        JobStatus jobStatus = null;
        if (jobstatusClient != null) {
            switch (status) {
                case ERROR:
                    executionData.executor.cleanup();
                    jobStatus = jobstatusClient.updateStatus(jobId, status, event, processedCount, errorCount);
                    break;
                case RESULTS_READY:
                    jobStatus = jobstatusClient.updateStatus(jobId, status, event, processedCount, errorCount);
                    break;
                case RUNNING:
                    jobStatus = jobstatusClient.updateStatus(jobId, status, event, processedCount, errorCount);
                    break;
            }
        } else {
            switch (status) {
                case ERROR:
                    executionData.executor.cleanup();
            }
            // no jobstatusClient to persist the status - probably in testing
            jobStatus = executionData.jobStatus.withStatus(status, processedCount, errorCount, event);

        }
        if (jobStatus != null) {
            executionData.jobStatus = jobStatus;
        }
        return jobStatus;
    }

    class ExecutionData {
        ExternalExecutor executor;
        JobStatus jobStatus;
    }

}
