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

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.camel.CamelContext;
import org.squonk.core.ServiceConfig;
import org.squonk.core.ServiceDescriptor;
import org.squonk.core.ServiceDescriptorToOpenAPIConverter;
import org.squonk.core.ServiceDescriptorUtils;
import org.squonk.core.client.JobStatusRestClient;
import org.squonk.io.IODescriptor;
import org.squonk.io.SquonkDataSource;
import org.squonk.jobdef.ExternalJobDefinition;
import org.squonk.jobdef.JobStatus;
import org.squonk.jobdef.JobStatus.Status;
import org.squonk.types.DefaultHandler;
import org.squonk.types.TypeResolver;
import org.squonk.util.IOUtils;

import javax.activation.DataSource;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    // probably should inject this
    private CamelContext camelContext;

    protected boolean sendStatus = Boolean.valueOf(
            IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_SEND_STATUS_UPDATES", "true"));
    private boolean loadInitialServiceDescriptors = Boolean.valueOf(
            IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_LOAD_INITIAL_SERVICE_DESCRIPTORS", "true"));
    private boolean loadTestServiceDescriptors = Boolean.valueOf(
            IOUtils.getConfiguration("SQUONK_JOBEXECUTOR_LOAD_TEST_SERVICE_DESCRIPTORS", "false"));


    private final Map<String, ExecutionData> executionDataMap = new LinkedHashMap<>();

    private final Map<String,ServiceDescriptor> serviceDescriptors = new HashMap<>();


    public JobManager() {
        initServiceDescriptors();
    }

    public JobManager(boolean loadInitialServiceDescriptors, boolean loadTestServiceDescriptors) {
        this.loadInitialServiceDescriptors = loadInitialServiceDescriptors;
        this.loadTestServiceDescriptors = loadTestServiceDescriptors;
        initServiceDescriptors();
    }

    private void initServiceDescriptors() {
        if (loadInitialServiceDescriptors) {
            LOG.info("Loading initial service descriptors");
            loadServiceDescriptors("service-descriptors-live.json");
        }
        if (loadTestServiceDescriptors) {
            LOG.info("Loading test service descriptors");
            loadServiceDescriptors("service-descriptors-test.json");
        }
    }

    private void loadServiceDescriptors(String resource) {
        try {
            InputStream json = this.getClass().getResourceAsStream(resource);
            if (json == null) {
                LOG.severe("Service descriptors not found at " + resource);
            } else {
                List<ServiceDescriptor> sds = ServiceDescriptorUtils.readJsonList(json);
                LOG.fine("Discovered " + sds.size() + " service descriptors");
                putServiceDescriptors(sds);
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to load service descriptors", ioe);
        }
    }

    public Collection<ServiceDescriptor> fetchServiceDescriptors() {
        return serviceDescriptors.values();
    }

    public OpenAPI fetchServiceDescriptorSwagger(String baseUrl) throws IOException {
        ServiceDescriptorToOpenAPIConverter converter = new ServiceDescriptorToOpenAPIConverter(baseUrl);
        OpenAPI oai = converter.convertToOpenApi(serviceDescriptors.values());
        return oai;
    }

    public List<Map<String,String>> fetchServiceDescriptorInfo() {
        List<Map<String,String>> results = new ArrayList<>();
        for (ServiceDescriptor sd : serviceDescriptors.values()) {
            Map<String,String> info = new LinkedHashMap<>(3);
            info.put("id", sd.getId());
            String name = sd.getServiceConfig().getName();
            if (name != null && !name.isEmpty()) {
                info.put("name", name);
            }
            String desc = sd.getServiceConfig().getName();
            if (desc != null && !desc.isEmpty()) {
                info.put("description", desc);
            }
            results.add(info);
        }
        return results;
    }

    public ServiceConfig fetchServiceConfig(String id) {
        ServiceDescriptor sd = serviceDescriptors.get(id);
        if (sd == null) {
            return null;
        } else {
            return sd.getServiceConfig();
        }
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void putServiceDescriptors(List<ServiceDescriptor> sds) {
        sds.forEach((sd) -> {
            LOG.fine("Adding service descriptor " + sd.getId());
            serviceDescriptors.put(sd.getId(), sd);
        });
        LOG.info("Added " + sds.size() + " ServiceDescriptors. Total is now " + serviceDescriptors.size());
    }

    /** Lookup the ExecutionData instance for the job with this ID and check that it belongs to the username
     *
     * @param username
     * @param jobId
     * @return The ExecutionData if it exists and is owned by the user, or if not then null
     */
    private ExecutionData findMyExecutionData(String username, String jobId) {
        ExecutionData executionData = executionDataMap.get(jobId);
        if (executionData == null) {
            return null;
        }
        return verifyUserOwnsJob(username, executionData.jobStatus) ? executionData : null;
    }

    /** Submit a new job. The call will return immediately with the JobStatus from which you can obtain the job's ID.
     * You then use that ID to check the status, and when the status changes to @{link JobStatus.Status.RESULTS_READY}
     * you can then fetch the results and then cleanup.
     *
     * @param serviceId
     * @param options
     * @param inputs
     * @param username
     * @return
     * @throws Exception
     */
    public JobStatus executeAsync(
            String username,
            String serviceId,
            Map<String,Object> options,
            Map<String, DataSource> inputs,
            String auth) throws Exception {

        return execute(username, serviceId, options, inputs, true, auth);
    }

    private JobStatus execute(
            String username,
            String serviceId,
            Map<String, Object> options,
            Map<String, DataSource> inputs,
            boolean async,
            String auth) throws Exception {

        if (inputs == null) {
            LOG.info("Executing with no inputs");
        } else {
            LOG.info("Executing with " + inputs.size() + "  inputs");
        }

        if (options == null) {
            options = Collections.emptyMap();
        }
        ServiceDescriptor serviceDescriptor = serviceDescriptors.get(serviceId);
        if (serviceDescriptor == null) {
            throw new IllegalStateException("No service descriptor found for " + serviceId);
        }

        ExternalJobDefinition jobDefinition = new ExternalJobDefinition(serviceDescriptor, options);
        LOG.info("Created JobDefinition with ID " + jobDefinition.getJobId());

        Map<String,Object> data = createObjectsFromDataSources(inputs, serviceDescriptor.resolveInputIODescriptors());
        LOG.info("Handling " + data.size() + " inputs");

        ExternalExecutor executor = new ExternalExecutor(jobDefinition, data, options, serviceDescriptor, camelContext, auth, this);
        LOG.fine("Executor job ID is " + executor.getJobId());
        JobStatus jobStatus = createJob(jobDefinition, username, 0);
        ExecutionData executionData = new ExecutionData();
        executionData.executor = executor;
        executionData.jobStatus = jobStatus;
        executionDataMap.put(executor.getJobId(), executionData);

        if (async) {
            LOG.info("Async execution of job " + executor.getJobId());
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
            jobStatus = updateStatus(executor.getJobId(), Status.RUNNING);
        } else {
            LOG.info("Sync execution of job " + executor.getJobId());
            executor.execute();
            jobStatus = updateStatus(executor.getJobId(), Status.RESULTS_READY);
        }

        return jobStatus;
    }

    protected Map<String,Object> createObjectsFromDataSources(Map<String, DataSource> inputs, IODescriptor[] ioDescriptors) throws Exception {
        // group the datasources by their input name
        Map<IODescriptor,Map<String,DataSource>> collectedDataSources = collectDataSources(inputs, ioDescriptors);
        // now convert them to objects of the type specified by the datasource
        Map<IODescriptor, Object> collectedObjects = collectObjects(collectedDataSources);
        // now convert the value types if necessary e.g. convert sdf to datasource
        Map<IODescriptor, Object> convertedObjects = convertObjects(collectedObjects);
        // generate the output
        Map<String,Object> results = new LinkedHashMap<>();
        for (Map.Entry<IODescriptor,Object> e : convertedObjects.entrySet()) {
            results.put(e.getKey().getName(), e.getValue());
        }
        return results;
    }

    /** The inputs map has keys that are the form fields. The value of the DataSource name that is the value of this key
     * depends on what is specified the Content-Disposition header so cannot be relied on.
     *
     * @param inputs
     * @param ioDescriptors
     * @return
     * @throws Exception
     */
    private Map<IODescriptor,Map<String,DataSource>> collectDataSources(Map<String, DataSource> inputs, IODescriptor[] ioDescriptors) throws Exception {
        Map<IODescriptor,Map<String,DataSource>> results = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<String, DataSource> e : inputs.entrySet()) {
            String dataSourceName = e.getKey();
            DataSource dataSource = e.getValue();
            String contentType = dataSource.getContentType();
            LOG.fine("Key: " + e.getKey() + " DataSourceName: " + dataSourceName + " ContentType: " + contentType);
            for (IODescriptor iod : ioDescriptors) {
                String inputName = iod.getName();
                if (inputName.equalsIgnoreCase(dataSourceName)) {
                    // exact match - an object that has a single InputStream
                    count++;
                    if (results.containsKey(iod)) {
                        throw new IllegalStateException("Duplicate DataSources found for " + inputName);
                    } else {
                        results.put(iod, Collections.singletonMap(dataSourceName, dataSource));
                        LOG.fine("Collected DataSource " + dataSourceName);
                    }
                } else if (dataSourceName.toLowerCase().startsWith(inputName + "_")) {
                    // object using multiple InputStream parts
                    // each one will be named like <name-from-iodescriptor>_<part-name>
                    count++;
                    if (results.containsKey(iod)) {
                        results.get(iod).put(dataSourceName, dataSource);
                    } else {
                        Map<String,DataSource> map = new LinkedHashMap<>();
                        map.put(dataSourceName, dataSource);
                        results.put(iod, map);
                    }
                    LOG.fine("Collected DataSource " + dataSourceName);
                }
            }
        }
        if (count != inputs.size()) {
            LOG.warning("Inconsistent DataSets. Found " + count + " that matched input descriptors but " + inputs.size() + " were present");
        }
        return results;
    }

    private Map<IODescriptor, Object> collectObjects(Map<IODescriptor,Map<String,DataSource>> inputs) throws Exception {
        if (inputs.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<IODescriptor, Object> results = new LinkedHashMap<>();
        for (Map.Entry<IODescriptor, Map<String,DataSource>> e : inputs.entrySet()) {
            IODescriptor iod = e.getKey();
            Map<String,DataSource> dataSources = e.getValue();

            LOG.fine("Building objects for " + dataSources.keySet().stream().collect(Collectors.joining(",")));
            Object value = null;
            if (dataSources.size() == 1) {
                DataSource ds = dataSources.values().iterator().next();
                IODescriptor dsIod = TypeResolver.getInstance().createIODescriptor(iod.getName(), ds.getContentType());
                value = DefaultHandler.buildObject(dsIod, Collections.singletonMap(iod.getName(), ds.getInputStream()));
                results.put(iod, value);
            } else if (dataSources.size() > 1) {
                Map<String, InputStream> inputStreams = new LinkedHashMap<>(dataSources.size());
                for (Map.Entry<String,DataSource> dse : dataSources.entrySet()) {
                    String fieldName = dse.getKey();
                    DataSource ds = dse.getValue();
                    String prefix = iod.getName() + "_";
                    if (fieldName.toLowerCase().startsWith(prefix)) {
                        String trimmed = fieldName.substring(prefix.length());
                        inputStreams.put(trimmed, ds.getInputStream());
                        LOG.fine("Put trimmed input " + trimmed);
                    } else {
                        throw new IllegalStateException("Unexpected DataSource name " + fieldName + " for input " + iod.getName());
                    }
                }
                value = DefaultHandler.buildObject(iod, inputStreams);
                results.put(iod, value);
            }

        }
        return results;
    }

    /** Convert the inputs to the types defined by the IODescriptor.
     *
     * @param inputs
     * @return
     */
    private Map<IODescriptor, Object> convertObjects(Map<IODescriptor, Object> inputs) {
        Map<IODescriptor, Object> results = new LinkedHashMap<>(inputs.size());
        for (Map.Entry<IODescriptor, Object> e : inputs.entrySet()) {
            IODescriptor iod = e.getKey();
            Object val = e.getValue();
            LOG.info("Testing " + val.getClass().getName() + " to " + iod.getPrimaryType().getName());
            if (iod.getPrimaryType() == val.getClass() || iod.getPrimaryType().isAssignableFrom(val.getClass())) {
                results.put(iod, val);
            } else {
                Object converted = camelContext.getTypeConverter().convertTo(iod.getPrimaryType(), val);
                if (converted == null) {
                    throw new IllegalStateException(String.format("Can't convert input %s to %s",
                            val.getClass().getName(), iod.getPrimaryType().getName()));
                } else {
                    LOG.info("Converted " + val.getClass().getName() + " to " + iod.getPrimaryType().getName());
                    results.put(iod, converted);
                }
            }
        }
        return results;
    }

    /** Get all the current jobs for the specified user.
     * This does not include jobs that have been cleaned up and removed.
     *
     * @param username
     * @return
     */
    public List<JobStatus> getJobs(String username) {
        if (username == null || username.isEmpty()) {
            LOG.warning("Username must be specified");
            return Collections.emptyList();
        }
        List<JobStatus> jobs = new ArrayList<>();
        executionDataMap.entrySet().forEach((e) -> {
            JobStatus jobStatus = e.getValue().jobStatus;
            if (username.equalsIgnoreCase(jobStatus.getUsername())) {
                jobs.add(jobStatus);
            }
        });
        LOG.fine("Found " + jobs.size() + " jobs");
        return jobs;
    }

    /** Get the current execution status of a job you submitted.
     *
     * @param jobId
     * @return
     */
    public JobStatus getJobStatus(String username, String jobId) {
        ExecutionData executionData = findMyExecutionData(username, jobId);
        if (executionData == null) {
            return null;
        }
        return executionData.jobStatus;
    }




    /** Get the results for a job. Only call this once the status is @{link JobStatus.Status.RESULTS_READY}.
     * Once you safely have the results make sure you call {@link #cleanupJob(String, String)}
     *
     * @param jobId
     * @return
     */
    public Map<String,Object> getJobResultsAsObjects(String username, String jobId) throws Exception {
        ExecutionData executionData = findMyExecutionData(username, jobId);
        if (executionData != null) {
            return executionData.executor.getResultsAsObjects();
        } else {
            return null;
        }
    }

    public Map<String,List<SquonkDataSource>> getJobResultsAsDataSources(String username, String jobId) throws Exception {
        ExecutionData executionData = findMyExecutionData(username, jobId);
        if (executionData != null) {
            return executionData.executor.getResultsAsDataSources();
        } else {
            return null;
        }
    }

    /** Must be called after the results have been fetched so that any execution artifacts (containers etc.) and data
     * files can be deleted.
     *
     * @param jobId
     */
    public JobStatus cleanupJob(String username, String jobId) {
        ExecutionData executionData = findMyExecutionData(username, jobId);
        if (executionData == null) {
            LOG.log(Level.INFO,
                    "Failed to find executiomn data (" +
                    username + ", " + jobId + ")");
            return null;
        }
        JobStatus jobStatus = null;
        ExternalExecutor executor = executionData.executor;
        if (executor != null) {
            try {
                LOG.log(Level.INFO, "Invoking executor.cleanup() for jobId " + jobId);
                executor.cleanup();
                // set the persisted status to complete
                jobStatus = updateStatus(jobId, JobStatus.Status.COMPLETED);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to update JobStatus", e);
                // should we retry later?
            } finally {
                // job complete and state persisted so we can remove it
                LOG.log(Level.INFO, "removing jobId " + jobId);
                executionDataMap.remove(jobId);
            }
        } else {
            LOG.log(Level.SEVERE,
                    "executor is null (" + username + ", " + jobId + ")");
        }
        return jobStatus;
    }

    public JobStatus cancelJob(String username, String jobId) {
        ExecutionData executionData = findMyExecutionData(username, jobId);
        if (executionData == null) {
            LOG.log(Level.INFO,
                    "Failed to find executiomn data (" +
                            username + ", " + jobId + ")");
            return null;
        }
        ExternalExecutor executor = executionData.executor;
        JobStatus jobStatus = null;
        if (executor != null) {
            try {
                LOG.log(Level.INFO, "Invoking executor.cancel() for jobId " + jobId);
                executor.cancel();
                // set the persisted status to cancelled
                jobStatus = updateStatus(jobId, Status.CANCELLED);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to update JobStatus", e);
                // should we retry later?
            } finally {
                // job cancelled and state persisted so we can remove it
                executionDataMap.remove(jobId);
            }
        }
        return jobStatus;
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
                    LOG.log(Level.INFO, "Trying to cleanup job " + jobStatus.getJobId());
                    executor.cleanup();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to cleanup job " + jobStatus.getJobId());
                }

                // set status to error
                try {
                    updateStatus(jobStatus.getJobId(), Status.ERROR, ExecutableJob.MSG_RESULTS_NOT_FETCHED, null, null);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to set job status to ERROR for job " + jobStatus.getJobId());
                }

                // purge - results can no longer be fetched
                executionDataMap.remove(jobStatus.getJobId());

            } else if (now - jobStatus.getStarted().getTime() > timeSinceStarted) {
                // job has been running for too long so we kill it

                // try to cancel the execution
                try {
                    LOG.log(Level.INFO, "Trying to cancel job " + jobStatus.getJobId());
                    executor.cancel();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to terminate job " + jobStatus.getJobId());
                }

                // set the status to error
                try {
                    updateStatus(jobStatus.getJobId(), Status.ERROR, ExecutableJob.MSG_JOB_TOOK_TOO_LONG, null, null);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Failed to set job status to ERROR for job " + jobStatus.getJobId());
                }

                // purge
                executionDataMap.remove(jobStatus.getJobId());

            }
        }
    }

    private JobStatus createJob(ExternalJobDefinition jobDefinition, String username, Integer totalCount) throws IOException {
        JobStatus jobStatus = null;
        if (sendStatus && jobstatusClient != null) {
            jobStatus = jobstatusClient.create(jobDefinition, username, totalCount);
            LOG.info("Creating Job with ID " + jobDefinition.getJobId());
        } else {
            jobStatus = JobStatus.create(jobDefinition.getJobId(), jobDefinition, username, new Date(), 0);
        }
        return jobStatus;
    }

    private JobStatus updateStatus(String jobId, Status status) throws IOException {
        return updateStatus(jobId, status, null, null, null);
    }

    /** Receive status updates through the callback interface
     *
     * @param jobId The ID of the job to update
     * @param status The current status
     * @param event A message that describes the event that caused the update.
     * @param processedCount The number of records processed so far.
     * @param errorCount The number or errors encountered so far.
     * @return
     * @throws IOException
     */
    @Override
    public JobStatus updateStatus(String jobId, Status status, String event, Integer processedCount, Integer errorCount)
            throws IOException {
        ExecutionData executionData = executionDataMap.get(jobId);
        if (executionData == null) {
            LOG.warning("Job ID " + jobId + " not found. Either it is incorrect or the job has completed");
            return null;
        }
        LOG.info("Updating Job status for job ID " + jobId);
        JobStatus jobStatus = null;
        if (sendStatus && jobstatusClient != null) {
            switch (status) {
                case ERROR:
                    executionData.executor.cleanup();
                    jobStatus = jobstatusClient.updateStatus(jobId, status, event, processedCount, errorCount);
                    break;
                default:
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
            LOG.info("Not sending status. Status is " + jobStatus);
        }
        if (jobStatus != null) {
            executionData.jobStatus = jobStatus;
        }
        return jobStatus;
    }

    private boolean verifyUserOwnsJob(String username, JobStatus jobStatus) {
        return username.equalsIgnoreCase(jobStatus.getUsername());
    }

    class ExecutionData {
        ExternalExecutor executor;
        JobStatus jobStatus;
    }

}
