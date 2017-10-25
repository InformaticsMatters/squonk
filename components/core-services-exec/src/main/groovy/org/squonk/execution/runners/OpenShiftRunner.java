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

package org.squonk.execution.runners;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.squonk.util.IOUtils;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An OpenShift-based Docker image executor that expects inputs and outputs.
 * The runner relies on the presence of a Persistent Volume Claim (PVC) that
 * has been successfully bound.
 * <p/>
 * In order to run this within an OpoenShift cluster you will need:
 * - A project (named "squonk")
 * - A suitable service account with admin privileges (named "squonk")
 *
 * @author Alan Christie
 */
public class OpenShiftRunner extends AbstractRunner {

    // TODO isRunning really an Enum?
    // TODO Still need to get container logs on failure

    private static final Logger LOG = Logger.getLogger(OpenShiftRunner.class.getName());

    private static final String WORK_DIR_ENV_NAME = "SQUONK_DOCKER_WORK_DIR";
    private static final String WORK_DIR_DEFAULT = "/squonk/work/docker";

    private static final String PROJECT_ENV_NAME = "SQUONK_PROJECT_NAME";
    private static final String PROJECT_DEFAULT = "squonk";

    private static final String SA_ENV_NAME = "SQUONK_SERVICE_ACCOUNT";
    private static final String SA_DEFAULT = "squonk";

    private static final String PVC_NAME_ENV_NAME = "SQUONK_WORK_DIR_PVC_NAME";
    private static final String PVC_NAME_DEFAULT = "squonk-runner-pvc";

    private static final String OBJ_BASE_NAME_ENV_NAME = "SQUONK_RUNNER_OBJ_BASE_NAME";
    private static final String OBJ_BASE_NAME_DEFAULT = "squonk-runner-job";

    private static final String OS_SA;
    private static final String OS_PROJECT;
    private static final String OS_OBJ_BASE_NAME;
    private static final String OS_DATA_VOLUME_PVC_NAME;
    private static final String OS_IMAGE_PULL_POLICY = "IfNotPresent";
    private static final String OS_JOB_RESTART_POLICY = "Never";

    // The OpenShift Job is given a period of time to start.
    // This time accommodates a reasonable time to pull the image
    // from an external repository. We need to do this
    // because I'm not sure, at the moment how to detect pull errors
    // from within OpenShift - so this is 'belt-and-braces' protection.
    private static final long JOB_START_GRACE_PERIOD_M = 5;

    // Time between checks on the 'job watcher' completion state.
    private static final long WATCHER_POLL_PERIOD_MILLIS = 1000;

    private static OpenShiftClient client;
    private static Watch watchObject;

    private final String localWorkDir;
    private String subPath;

    private String jobName;
    private String imageName;

    private boolean isExecuting;
    private boolean stopRequested;
    private boolean jobCreated;

    /**
     * The JobWatcher receives events form the launched Job
     * and is responsible for determining when the executed Job has
     * run to completion.
     */
    static class JobWatcher implements Watcher<Job> {

        private String jobName;
        private boolean jobStarted;     // True when the Job has started
        private boolean jobComplete;    // True when the Job has stopped
        private boolean success;        // True if the Job stopped without error

        /**
         * Basic constructor.
         *
         * @param jobName The symbolic name assigned to the job.
         */
        JobWatcher(String jobName) {
            this.jobName = jobName;
        }

        /**
         * Returns true when the watched Job has run to completion
         * (with or without an error).
         *
         * @return True on completion.
         */
        boolean jobComplete() {
            return jobComplete;
        }

        /**
         * Returns true when the watched Job has run to completion
         * (with or without an error).
         *
         * @return True on completion.
         */
        boolean jobStarted() {
            return jobStarted;
        }

        /**
         * Returns true if the Job ended successfully.
         *
         * @return True on completion.
         */
        boolean success() {
            return success;
        }

        @Override
        public void eventReceived(Action action, Job resource) {

            LOG.log(Level.FINE, resource.toString());

            JobStatus jobStatus = resource.getStatus();
            if (jobStatus.getStartTime() == null) {

                // StartTime has not been set
                // (Job must still be loading)
                LOG.info(jobName + " is starting...");

            } else if (jobStatus.getCompletionTime() == null) {

                // CompletionTime has not been set
                // (Job must still be running)
                LOG.info(jobName + " is running...");
                jobStarted = true;

            } else {

                // TODO Can we get the exit code of the Job?

                // StartTime and CompletionTime are set.
                // The job must be complete.
                // Ignore subsequent stops,
                // which occur when we delete the Job.
                if (!jobComplete) {
                    success = jobStatus.getSucceeded() > 0;
                    LOG.info(jobName + " has stopped. Success=" + success);
                    jobComplete = true;
                }

            }

        }

        @Override
        public void onClose(KubernetesClientException cause) {

            if (cause != null) {
                cause.printStackTrace();
                LOG.log(Level.SEVERE, cause.getMessage(), cause);
            }
            jobComplete = true;

        }

    }

    /**
     * Creates an OpenShift runner instance. Normally followed by a call to
     * init() and then execute().
     *
     * @param imageName       The Docker image to run.
     * @param hostBaseWorkDir The directory on the host that will be used to
     *                        create a work dir. It must exist or be creatable
     *                        and be writeable. This is typically
     *                        `/squonk/work/docker/[uuid]`.
     * @param localWorkDir    The name under which the host work dir will be
     *                        mounted in the new container. Typically `/work`.
     */
    public OpenShiftRunner(String imageName, String hostBaseWorkDir, String localWorkDir) {

        super(hostBaseWorkDir);

        LOG.info("imageName='" + imageName + "'" +
                 " hostBaseWorkDir='" + hostBaseWorkDir + "'" +
                 " localWorkDir=" + localWorkDir);

        this.imageName = imageName;
        this.localWorkDir = localWorkDir;

        // The host base directory's leaf directory
        // will be used as a 'sub-path' for mounting the Job.

        // TODO Is BaseWorkDir and subPath going to work.
        //      Document with concrete example...

        // Break up the path to get a sub-directory.
        // We expect to be given '/parent/child' so we expect to find
        // more than one '/' and we want the parent and child.
        // The child becomes the sub-path.
        int lastSlash = hostBaseWorkDir.lastIndexOf("/");
        if (lastSlash < 1) {
            // We expect this to be greater and 0!
            // Leaving now will cause us to fail our execution-time tests.
            return;
        }
        subPath = hostBaseWorkDir.substring(lastSlash + 1);
        if (subPath.length() == 0) {
            // We expect a sub-path.
            // Leaving now will cause us to fail our execution-time tests.
            return;
        }

        // Form the string that will be used to name all our OS objects...
        // TODO is subPath suitable or do we append our own unique value?
        jobName = String.format("%s-%s", OS_OBJ_BASE_NAME, subPath);
        LOG.info("jobName='" + jobName + "'");

    }

    /**
     * Alternative constructor, creates an OpenShift runner instance.
     * The directory mounted in the container is based on the default working
     * directory.
     *
     * @param imageName The Docker image to run.
     */
    public OpenShiftRunner(String imageName) {

        super(null);

        LOG.fine("imageName='" + imageName +"'");

        this.imageName = imageName;
        this.localWorkDir = getHostWorkDir().getPath();

        // TODO Not sure how to handle sub-path here!

    }

    /**
     * Initialise the runner. This is called to allow the execution of
     * time-consuming post-creation operations to take place. This method
     * must be called before a call to execute().
     * <p/>
     * The method should not be called more than once.
     */
    public synchronized void init() {

        LOG.fine("Initialising...");

        // Only permitted on initial (created) state
        if (isRunning != RUNNER_CREATED) {
            LOG.warning("Call to init() when initialised");
        }

        // For now, there's nothing really to initialise.
        // The method is here to comply with protocol.
        // The execute() method creates and prepares the dependent objects.

        LOG.fine("Initialised.");

        isRunning = RUNNER_INITIALISED;

    }

    /**
     * Executes the container image and blocks until the container
     * (an OpenShift 'Job') has completed. This method must only be called
     * once, subsequent calls are ignored.
     * <p/>
     * The runner instance must have been initialised before calling
     * execute() otherwise an error will be immediately returned.
     *
     * @param cmd The command sequence to run in the container
     * @return Execution status (non-zero on error)
     *
     * @throws IllegalStateException if the method is called incorrectly
     *         or if there are problems instantiating the Job.
     */
    public synchronized int execute(String... cmd)
            throws IllegalStateException {

        // Must be coming from the initialised state.
        if (isRunning != RUNNER_INITIALISED) {
            throw new IllegalStateException("execute() with bad isRunning state (" + isRunning + ")");
        }
        // Sub-path properly formed?
        if (subPath == null || subPath.length() == 0) {
            throw new IllegalStateException("execute() with missing subPath");
        }

        LOG.fine("Executing...");

        isRunning = RUNNER_RUNNNG;
        isExecuting = true;
        int containerExitStatus = 0;

        // ---

        // Create the objects required to start the job...

        // Volume
        PersistentVolumeClaimVolumeSource pvcSrc =
                new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName(OS_DATA_VOLUME_PVC_NAME)
                        .withReadOnly(false).build();
        Volume volume = new VolumeBuilder()
                .withName(jobName)
                .withPersistentVolumeClaim(pvcSrc).build();

        // Volume Mount
        VolumeMount volumeMount = new VolumeMountBuilder()
                .withMountPath(localWorkDir)
                .withName(jobName)
                .withSubPath(subPath).build();

        // Container (that will run in the Job)
        Container jobContainer = new ContainerBuilder()
                .withName(jobName)
                .withImage(imageName)
                .withCommand(cmd)
                .withImagePullPolicy(OS_IMAGE_PULL_POLICY)
                .withVolumeMounts(volumeMount).build();

        // The Job, which runs the container image...
        Job job = new JobBuilder()
                .withNewMetadata()
                .withName(jobName)
                .withNamespace(OS_PROJECT)
                .endMetadata()
                .withNewSpec()
                .withNewTemplate()
                .withNewSpec()
                .withContainers(jobContainer)
                .withServiceAccount(OS_SA)
                .withRestartPolicy(OS_JOB_RESTART_POLICY)
                .withVolumes(volume)
                .endSpec()
                .endTemplate()
                .endSpec().build();

        // Create a Job 'watcher'
        // to monitor the Job execution progress...

        JobWatcher jobWatcher = new JobWatcher(jobName);
        watchObject = client.extensions().jobs()
                .withName(jobName)
                .watch(jobWatcher);

        // Launch...

        try {
            client.extensions().jobs().create(job);
        } catch (KubernetesClientException ex) {

            // Nothing to do other than log, cleanup and re-throw...
            LOG.severe("KubernetesClientException creating the Job: " + ex.getMessage());
            cleanUp();

            throw new IllegalStateException("Exception creating Job", ex);

        }
        // We should have a Job.
        // Setting this flag allows cleanUp() to clean it up.
        jobCreated = true;

        // Wait...

        long jobStartTimeMillis = System.currentTimeMillis();
        boolean jobStartFailure = false;
        while (!jobStartFailure
               && !jobWatcher.jobComplete()
               && !stopRequested) {

            // Pause...
            try {
                Thread.sleep(WATCHER_POLL_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!jobWatcher.jobStarted()) {
                // Have we waited too long for the Job to start?
                long now = System.currentTimeMillis();
                long elapsedMins = (now - jobStartTimeMillis) / 60000;
                if (elapsedMins >= JOB_START_GRACE_PERIOD_M) {
                    LOG.log(Level.WARNING, "Job failed to start. Leaving.");
                    jobStartFailure = true;
                }
            }

        }
        if (!jobStartFailure && !stopRequested) {
            LOG.log(Level.INFO, "Job exitStatus=" + jobWatcher.success());
        }

        // TODO CONTAINER EXIT STATUS AND LOG!

        // Always clean up.
        // This cancels the JobWatcher.
        cleanUp();

        // ---

        LOG.fine("Executed.");

        // Adjust the 'isRunning' state if we terminated witout being stopped.
        // If we're stopped the 'stop()' method is responsible for the
        // isRunning value.
        if (!stopRequested) {
            isRunning = RUNNER_FINISHED;
        }

        // No longer executing...
        // The 'stop()' method (if used) will be waiting for this.
        isExecuting = false;

        return containerExitStatus;

    }

    @Override
    public String getLog() {
        return "";
    }

    @Override
    public com.github.dockerjava.api.model.Volume addVolume(String mountAs) {
        throw new UnsupportedOperationException("OpenShiftRunner does not support Volumes");
    }

    @Override
    public Bind addBind(String hostDir, com.github.dockerjava.api.model.Volume volume, AccessMode mode) {
        throw new UnsupportedOperationException("OpenShiftRunner does not support Bind");
    }

    /**
     * Returns the defined Docker image working directory, or a default
     * if the expected environment variable has not been set.
     *
     * @return The image working directory
     */
    @Override
    protected String getDefaultWorkDir() {

        return IOUtils.getConfiguration(WORK_DIR_ENV_NAME, WORK_DIR_DEFAULT);

    }

    /**
     * Called to prematurely stop the execution of the running container.
     * This method blocks until the container image has terminated.
     * The container exit code it not of interest.
     */
    @Override
    public synchronized void stop() {

        // The only sensible running state is RUNNING.
        // Any other state can be ignored.
        if (isRunning != RUNNER_RUNNNG) {
            return;
        }

        isRunning = RUNNER_STOPPING;

        LOG.fine("Stopping...");

        // Setting 'stopRequested' should cause the 'execute()'
        // method  to complete. We wait here until it does.
        stopRequested = true;
        while (isExecuting) {

            // Pause
            try {
                Thread.sleep(WATCHER_POLL_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                // Don't care
            }

        }

        LOG.fine("Stopped.");

        isRunning = RUNNER_STOPPED;

    }

    /**
     * Cleans up the runner. This method must be called when a runner
     * execution completes, with or without error. It is responsible for
     * removing OpenShift objects and stopping the JobWatcher.
     */
    private void cleanUp() {

        LOG.fine("Cleaning up...");

        if (client == null || jobName == null) {
            // Probably nothing to clean up...
            return;
        }

        // The Job may have failed to get created.
        // Clean it up if we think it was created.
        if (jobCreated) {
            LOG.fine("...Job");
            client.extensions().jobs()
                    .inNamespace(OS_PROJECT)
                    .withName(jobName)
                    .cascading(true)
                    .withGracePeriod(0)
                    .delete();
            LOG.fine("...Pod");
            client.pods()
                    .inNamespace(OS_PROJECT)
                    .withLabel("job-name", jobName)
                    .delete();
        }

        // There's always a JobWatcher
        if (watchObject != null) {
            LOG.fine("...Watcher");
            watchObject.close();
        }

        LOG.fine("Cleaned.");

    }

    static {

        // Create the OpenShift client...
        LOG.fine("Creating DefaultOpenShiftClient...");
        client = new DefaultOpenShiftClient();
        LOG.fine("Created.");

        // Get the preferred service account...
        OS_SA = IOUtils
                .getConfiguration(SA_ENV_NAME, SA_DEFAULT);

        // Get the preferred project name...
        OS_PROJECT = IOUtils
                .getConfiguration(PROJECT_ENV_NAME, PROJECT_DEFAULT);

        // Get the PVC name of the data mount...
        OS_DATA_VOLUME_PVC_NAME = IOUtils
                .getConfiguration(PVC_NAME_ENV_NAME, PVC_NAME_DEFAULT);

        // And the base-name for all Jobs we create...
        OS_OBJ_BASE_NAME = IOUtils
                .getConfiguration(OBJ_BASE_NAME_ENV_NAME, OBJ_BASE_NAME_DEFAULT);

    }

}