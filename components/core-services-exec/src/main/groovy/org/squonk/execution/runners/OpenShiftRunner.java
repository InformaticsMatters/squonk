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
import com.google.common.collect.EvictingQueue;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.squonk.util.IOUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Map;

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

    // SQUONK_CELL_JOB_PROJECT_NAME defines the name of the
    // project/namespace that the generated Jobs are created in.
    private static final String PROJECT_ENV_NAME = "SQUONK_CELL_JOB_PROJECT_NAME";
    private static final String PROJECT_DEFAULT = "squonk";

    private static final String SA_ENV_NAME = "SQUONK_SERVICE_ACCOUNT";
    private static final String SA_DEFAULT = "squonk";

    private static final String LOG_HISTORY_ENV_NAME = "SQUONK_POD_LOG_HISTORY";
    private static final String LOG_HISTORY_DEFAULT = "0";

    private static final String PVC_NAME_ENV_NAME = "SQUONK_WORK_DIR_PVC_NAME";
    private static final String PVC_NAME_DEFAULT = "squonk-work-dir-pvc";

    private static final String POD_BASE_NAME_ENV_NAME = "SQUONK_POD_BASE_NAME";
    private static final String POD_BASE_NAME_DEFAULT = "squonk-cell-pod";

    // The Pod Environment is a string that consists of YAML name:value pairs.
    // If the environment string is present, each name/value pair is injected
    // as a separate environemnt variable into the Pod container.
    private static final String POD_ENVIRONMENT_ENV_NAME = "SQUONK_POD_ENVIRONMENT";
    private static final String POD_ENVIRONMENT_DEFAULT = "";

    private static final String OS_SA;
    private static final String OS_PROJECT;
    private static final String OS_POD_BASE_NAME;
    private static final String OS_POD_ENVIRONMENT;
    private static final String OS_DATA_VOLUME_PVC_NAME;
    private static final String OS_IMAGE_PULL_POLICY = "IfNotPresent";
    private static final String OS_POD_RESTART_POLICY = "Never";

    public static final int LOG_HISTORY;

    // The OpenShift Job is given a period of time to start.
    // This time accommodates a reasonable time to pull the image
    // from an external repository. We need to do this
    // because I'm not sure, at the moment how to detect pull errors
    // from within OpenShift - so this is 'belt-and-braces' protection.
    private static final long POD_START_GRACE_PERIOD_M = 15;

    // Time between checks on the 'job watcher' completion state.
    private static final long WATCHER_POLL_PERIOD_MILLIS = 1000;

    private static OpenShiftClient client;
    private static Watch watchObject;
    private static LogWatch logWatch;
    private static LogStream logStream;

    private final String hostBaseWorkDir;
    private final String localWorkDir;
    private String subPath;

    private String podName;
    private String imageName;

    private boolean isExecuting;
    private boolean stopRequested;
    private boolean podCreated;

    private Yaml yaml = new Yaml();

    /**
     * The PodWatcher receives events form the launched Pod
     * and is responsible for determining when the executed Pod has
     * run to completion.
     */
    static class PodWatcher implements Watcher<Pod> {

        private String podName;

        // We maintain a number of phases throughout the Pod's lifetime:
        // - "Waiting" (Initial state)
        // - "Starting"
        // - "Complete" (Stopped, waiting for the exit code event)
        // - "Finished" (where the exit code is available)
        private String podPhase = "Waiting";
        // The exit code of the Pod's container
        // Assigned when the container has terminated.
        private int containerExitCode = 1;

        /**
         * Basic constructor.
         *
         * @param podName The symbolic name assigned to the pod.
         */
        PodWatcher(String podName) {
            this.podName = podName;
        }

        /**
         * Returns true when the watched Pod has run to completion
         * (with or without an error).
         *
         * @return True on completion.
         */
        boolean podFinished() {
            return podPhase.equals("Finished");
        }

        /**
         * Returns true when the watched Pod has run to completion
         * (with or without an error).
         *
         * @return True on completion.
         */
        boolean podStarted() {
            return !podPhase.equals("Waiting");
        }

        /**
         * Returns the Pod Exit code.
         *
         * @return True on completion.
         */
        int exitCode() {
            return containerExitCode;
        }

        @Override
        public void eventReceived(Action action, Pod resource) {

            PodStatus podStatus = resource.getStatus();
            // For debug (it's a large object)...
//            LOG.info("podStatus=" + podStatus.toString());

            // Check each PodCondition and its ContainerStatus array...
            // Significant information is:
            //   PodCondition.reason is PodCompleted (That's good)
            // Or
            //   Any ContainerStatus.state
            //      where the state is terminated
            //      when we'll find an exitCode and reason
            List<PodCondition> podConditions = podStatus.getConditions();
            if (podConditions != null) {
                for (PodCondition podCondition : podConditions) {
                    String conditionReason = podCondition.getReason();
                    if (conditionReason != null) {
                        LOG.info("conditionReason=" + conditionReason);
                        if (conditionReason.equals("PodCompleted")) {
                            podPhase = "Complete";
                            break;
                        }
                    }
                }
            }
            if (!podPhase.equals("Complete")) {
                List<ContainerStatus> containerStatuses = podStatus.getContainerStatuses();
                if (containerStatuses != null) {
                    for (ContainerStatus containerStatus : containerStatuses) {
                        ContainerState cs = containerStatus.getState();
                        if (cs != null) {
                            ContainerStateTerminated csTerm = cs.getTerminated();
                            if (csTerm != null) {
                                // The Pod's terminated (unexpectedly?)
                                // We'll handle the exit code in the next block.
                                LOG.info("Pod has Terminated" +
                                        " (exitCode=" + csTerm.getExitCode() +
                                        " reason='" + csTerm.getReason() + "')");
                                podPhase = "Complete";
                                break;
                            }
                        }
                    }
                }
            }
            // Waiting for
            if (podPhase == "Complete") {

                // If we don't have a LogStream object (used to capture the
                // launched pod's output) then create one now. With the current
                // Kubernetes library we must wait until now (when we know the
                // Pod's running) otherwise we won't be given any.
                if (logWatch == null) {
                    LOG.info(podName + " (Creating LogStream)");
                    logStream = new LogStream();
                    logWatch = client.pods()
                            .inNamespace(OS_PROJECT)
                            .withName(podName)
                            .watchLog(logStream);
                }

                // We're waiting for a ContainerStateTerminated record.
                // That will contain start/finish times and an exit code.
                List<ContainerStatus> containerStatuses = podStatus.getContainerStatuses();
                if (containerStatuses != null) {
                    for (ContainerStatus containerStatus : containerStatuses) {
                        // Is there a 'waiting' record?
                        ContainerStateWaiting csWaiting = containerStatus.getState().getWaiting();
                        if (csWaiting != null && csWaiting.getReason().equals("ContainerCreating")) {
                            podPhase = "Starting";
                        }
                        // Do we have a terminated record?
                        ContainerStateTerminated csTerm = containerStatus.getState().getTerminated();
                        if (csTerm != null) {
                            String startTimeStr = csTerm.getStartedAt();
                            String finishTimeStr = csTerm.getFinishedAt();
                            containerExitCode = csTerm.getExitCode();
                            LOG.info("podName=" + podName +
                                     " Terminated (" + startTimeStr + " - " + finishTimeStr + ")");
                            LOG.info("podName=" + podName +
                                     " containerExitCode=" + containerExitCode);
                            podPhase = "Finished";
                        }
                    }
                }
            }

            LOG.info("podName=" + podName + " podPhase=" + podPhase);

        }

        @Override
        public void onClose(KubernetesClientException cause) {

            if (cause != null) {
                cause.printStackTrace();
                LOG.severe("podName=" + podName +
                           " cause.message=" + cause.getMessage());
            }
            podPhase = "Finished";

        }

    }

    /**
     * The LogStream is used to collect the stdout from the launhced Pod.
     * It is created from within the PodWatcher when we're confident the
     * Pod's running.
     */
    static class LogStream extends OutputStream {

        // To accumulated the captured Pod's output...
        private StringBuilder stringBuilder = new StringBuilder();
        private Queue<String> logQueue = EvictingQueue.create(LOG_HISTORY);

        private void capture(byte[] b, int off, int len) {
            String logLine = new String(b, off, len);
            if (LOG_HISTORY == 0) {
                stringBuilder.append(logLine);
            } else {
                logQueue.add(logLine);
            }
        }

        @Override
        public void write(int b) {
            // Not really interested.
            // Nothing useful comes through here.
            LOG.warning("JOB-LOG [Got call to write(int)]");
        }

        @Override
        public void write(byte[] b, int off, int len) {
            capture(b, off, len);
        }

        @Override
        public void write(byte[] b) {
            capture(b, 0, b.length);
        }

        // Returns the accumulated Pod output
        // (may be an empty string)
        public String getCollectedOutput() {
            // If we were using a queue to limit the number
            // of log lines - iterate through it into our StringBuilder.
            // If we wern't using the logQueue then the StrignBuilder
            // will already contain our log lines...
            for (String line : logQueue) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
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
     * @param jobId The unique ID (uuid) assigned to the Cell job.
     *              This will be used to create a sub-directory in
     *              hostBaseWorkDir into which the input data is copied
     *              prior to running the job in a Pod. The Job's uuid is not
     *              necessarily the same as the uuid found in the
     *              `hostBaseWorkDir`
     */
    public OpenShiftRunner(String imageName, String hostBaseWorkDir, String localWorkDir, String jobId) {

        super(hostBaseWorkDir, jobId);

        LOG.info("imageName='" + imageName + "'" +
                 " hostBaseWorkDir='" + hostBaseWorkDir + "'" +
                 " localWorkDir='" + localWorkDir + "'" +
                 " jobId='" + jobId + "'");

        this.imageName = imageName;

        // Append 'latest' if tag's not specified.
        if (!this.imageName.contains(":")) {
            LOG.warning("Image has no tag. Adding ':latest'");
            this.imageName += ":latest";
        }

        // The host base directory's leaf directory
        // will be used as a 'sub-path' for mounting into the Pod.
        // If the paths are not provided try to obtain them
        // from underlying defaults. If that fails ... leave

        if (hostBaseWorkDir == null) {
            LOG.info("Null hostBaseWorkDir, trying getHostWorkDir()...");
            hostBaseWorkDir = getHostWorkDir().getPath();
        }
        this.hostBaseWorkDir = hostBaseWorkDir;

        if (localWorkDir == null) {
            LOG.info("Null localWorkDir, trying getDefaultWorkDir()...");
            localWorkDir = getDefaultWorkDir();
        }
        this.localWorkDir = localWorkDir;

        // Paths now?

        LOG.info("hostBaseWorkDir='" + this.hostBaseWorkDir + "'");
        LOG.info("localWorkDir='" + this.localWorkDir + "'");
        if (this.hostBaseWorkDir == null) {
            LOG.severe("Null hostBaseWorkDir");
            return;
        } else if (this.localWorkDir == null) {
            LOG.severe("Null localWorkDir");
            return;
        }

        // The host working directory is typically something like
        // '/squonk/work/docker/41b47633-a2e6-49ab-b32c-9ab19caf4d4c'
        // where the final directory is an automatically assigned UUID.
        // We use the final directory as the sub-path, which is where
        // the Pod we launch will be mounted.

        // Break up the path to get a sub-directory.
        // We expect to be given '/parent/child' so we expect to find
        // more than one '/' and we want the parent and child.
        // The child becomes the sub-path.
        int lastSlash = hostBaseWorkDir.lastIndexOf("/");
        if (lastSlash < 1) {
            // We expect this to be greater than 0!
            // Leaving now will cause us to fail our execution-time tests.
            LOG.severe("Could not find a subPath");
            return;
        }
        subPath = hostBaseWorkDir.substring(lastSlash + 1);
        if (subPath.length() == 0) {
            // We expect a sub-path.
            // Leaving now will cause us to fail our execution-time tests.
            LOG.severe("No subPath");
            return;
        }
        LOG.info("subPath='" + subPath + "'");

        // Form the string that will be used to name all our OS objects...
        podName = String.format("%s-%s", OS_POD_BASE_NAME, subPath);
        LOG.info("podName='" + podName + "'");

    }

    /**
     * Given an existing configuration string this method adds material
     * to allow a Nextflow container to run properly in our OpenShift
     * environment. This mainly consists of exposing the name of the
     * shared data volume claim, a suitable mount path, the sub-path
     * we're using and the service account for the namespace we're in.
     *
     * @param originalConfig The original configuration string.
     *                       If this is null, null is returned.
     * @return originalConfig with further config appended.
     */
    public String addExtraNextflowConfig(String originalConfig) {

        if (originalConfig == null) {
            return null;
        }

        // Add all the stuff Nextflow needs from our OpenShift world...
        // The following requires Nextflow 0.31.0 or later.
        String additionalConfig = String.format(
                "k8s {\n" +
                "  storageClaimName = '%s'\n" +
                "  storageMountPath = '%s'\n" +
                "  storageSubPath = '%s'\n" +
                "  serviceAccount = '%s'\n" +
                "}\n",
                OS_DATA_VOLUME_PVC_NAME,
                localWorkDir,
                jobId,
                OS_SA);

        return originalConfig + "\n" + additionalConfig;

    }

    /**
     * Initialise the runner. This is called to allow the execution of
     * time-consuming post-creation operations to take place. This method
     * must be called before a call to execute().
     * <p/>
     * The method should not be called more than once.
     */
    public synchronized void init() throws IOException {

        super.init();

        LOG.fine("Initialising (hostBaseWorkDir=" + hostBaseWorkDir + ")");

        // Only permitted on initial (created) state
        if (isRunning != RUNNER_CREATED) {
            LOG.warning(podName + " Call to init() when initialised");
        }

        // For now, there's nothing really to initialise.
        // The method is here to comply with protocol.
        // The execute() method creates and prepares the dependent objects.

        LOG.fine("Initialised");

        isRunning = RUNNER_INITIALISED;

    }

    /**
     * Executes the container image and blocks until the container
     * (an OpenShift 'Pod') has completed. This method must only be called
     * once, subsequent calls are ignored.
     * <p/>
     * The runner instance must have been initialised before calling
     * execute() otherwise an error will be immediately returned.
     *
     * @param cmd The command sequence to run in the container
     * @return Execution status (non-zero on error)
     *
     * @throws IllegalStateException if the method is called incorrectly
     *         or if there are problems instantiating the Pod.
     */
    public synchronized int execute(String... cmd)
            throws IllegalStateException {

        // Must be coming from the initialised state.
        if (isRunning != RUNNER_INITIALISED) {
            throw new IllegalStateException(podName +
                    " execute() with bad isRunning state (" + isRunning + ")");
        }
        // Sub-path properly formed?
        if (subPath == null || subPath.length() == 0) {
            throw new IllegalStateException(podName +
                    " execute() with missing subPath");
        }

        LOG.info(podName + " (Preparing to execute)");

        isRunning = RUNNER_RUNNNG;
        isExecuting = true;
        int containerExitCode = 0;

        // ---

        // Create the objects required to start the Pod...

        // Volume
        PersistentVolumeClaimVolumeSource pvcSrc =
                new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName(OS_DATA_VOLUME_PVC_NAME)
                        .withReadOnly(false).build();
        Volume volume = new VolumeBuilder()
                .withName(podName)
                .withPersistentVolumeClaim(pvcSrc).build();

        // Volume Mount
        VolumeMount volumeMount = new VolumeMountBuilder()
                .withMountPath(localWorkDir)
                .withName(podName)
                .withSubPath(subPath).build();

        // Create environment variables for the container.
        // This array is driven by the content of the
        // OS_POD_ENVIRONMENT YAML string of <NAME>=<VALUE> pairs.
        List<EnvVar> containerEnv = new ArrayList<>();
        if (OS_POD_ENVIRONMENT.length() > 0) {
            Map<String, Object> map =
                    (Map<String, Object>) yaml.load(OS_POD_ENVIRONMENT);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String name = entry.getKey().trim();
                String value = entry.getValue().toString().trim();
                LOG.info("...adding EnvVar " + name);
                containerEnv.add(new EnvVar(name, value, null));
            }
        }
        LOG.info("Number of OS_POD_ENVIRONMENT variables: " + containerEnv.size());

        // Set the container's profile (a nextflow feature) via an expected variable.
        // Here we're always running in a kubernetes enviornment.
        containerEnv.add(new EnvVar("NF_PROFILE_NAME", "kubernetes", null));

        // Container (that will run in the Pod)
        Container podContainer = new ContainerBuilder()
                .withName(podName)
                .withImage(imageName)
                .withWorkingDir(localWorkDir)
                .withImagePullPolicy(OS_IMAGE_PULL_POLICY)
                .withEnv(containerEnv)
                .withVolumeMounts(volumeMount).build();

        // The Pod, which runs the container image...
        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName(podName)
                .withNamespace(OS_PROJECT)
                .endMetadata()
                .withNewSpec()
                .withContainers(podContainer)
                .withServiceAccount(OS_SA)
                .withRestartPolicy(OS_POD_RESTART_POLICY)
                .withVolumes(volume)
                .endSpec().build();


        // Create a Pod 'watcher'
        // to monitor the Pod execution progress...

        LOG.info(podName + " (Creating PodWatcher)");
        PodWatcher podWatcher = new PodWatcher(podName);
        try {
            watchObject = client.pods()
                    .withName(podName)
                    .watch(podWatcher);
        } catch (KubernetesClientException ex) {

            // Nothing to do other than log, cleanup and re-throw...
            LOG.severe("KubernetesClientException creating PodWatcher : " +
                       ex.getMessage());
            cleanup();
            isRunning = RUNNER_FINISHED;

            throw new IllegalStateException("Exception creating PodWatcher", ex);

        }

        // Launch...

        LOG.info(podName + " (Creating Pod)");
        try {
            client.pods().create(pod);
        } catch (KubernetesClientException ex) {

            // Nothing to do other than log, cleanup and re-throw...
            LOG.severe("KubernetesClientException creating " + podName +
                       " : " + ex.getMessage());
            cleanup();
            isRunning = RUNNER_FINISHED;

            throw new IllegalStateException("Exception creating " + podName, ex);

        }
        // We should have a Pod.
        LOG.info(podName + " (Created)");
        // Setting this flag allows cleanUp() to clean it up.
        podCreated = true;

        // Wait...

        LOG.info(podName + " (Waiting for Pod completion)");
        long podStartTimeMillis = System.currentTimeMillis();
        boolean podStartFailure = false;
        while (!podStartFailure
               && !podWatcher.podFinished()
               && !stopRequested) {

            // Pause...
            try {
                Thread.sleep(WATCHER_POLL_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!podWatcher.podStarted()) {
                // Have we waited too long for the Pod to start?
                long now = System.currentTimeMillis();
                long elapsedMins = (now - podStartTimeMillis) / 60000;
                if (elapsedMins >= POD_START_GRACE_PERIOD_M) {
                    LOG.warning(podName + " failed to start. Leaving.");
                    podStartFailure = true;
                }
            }

        }
        if (!podStartFailure && !stopRequested) {
            containerExitCode = podWatcher.exitCode();
            LOG.info(podName + " containerExitCode=" + containerExitCode);
        }

        LOG.info(podName + " (Execute complete)");

        // ---

        // Adjust the 'isRunning' state if we terminated witout being stopped.
        // If we're stopped the 'stop()' method is responsible for the
        // isRunning value.
        if (!stopRequested) {
            isRunning = RUNNER_FINISHED;
        }

        // No longer executing...
        // The 'stop()' method (if used) will be waiting for this.
        isExecuting = false;

        return containerExitCode;

    }

    @Override
    public String getLog() {
        if (logStream == null) {
            return "";
        }
        return logStream.getCollectedOutput();
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
     * Called to prematurely stop the execution of the running container.
     * This method blocks until the container image has terminated.
     * The container exit code is not of interest here.
     */
    @Override
    public synchronized void stop() {

        // The only sensible running state is RUNNING.
        // Any other state can be ignored.
        if (isRunning != RUNNER_RUNNNG) {
            return;
        }

        isRunning = RUNNER_STOPPING;

        LOG.fine(podName + " (Stopping)");

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

        LOG.fine(podName + " (Stopped)");

        isRunning = RUNNER_STOPPED;

    }

    /**
     * Returns the local working directory.
     *
     * @return Path to the local working directory.
     */
    public String getLocalWorkDir() {
        return localWorkDir;
    }

    /**
     * Cleans up the runner. This method must be called when a runner
     * execution completes, with or without error. It is responsible for
     * removing OpenShift objects and stopping the JobWatcher.
     */
    public void cleanup() {

        LOG.fine(podName + " (Cleaning up)");

        // Clean up stuff that looks like it needs cleaning up...

        // The Job may have failed to get created.
        if (podCreated) {
            LOG.fine(podName + " (...Pod)");
            client.pods()
                    .inNamespace(OS_PROJECT)
                    .withName(podName)
                    .delete();
        }

        // There may not be a PodWatcher, LogWatcher and LogStream
        if (watchObject != null) {
            watchObject.close();
        }
        if (logWatch != null) {
            logWatch.close();
        }
        if (logStream != null) {
            logStream = null;
        }

        super.cleanup();
        deleteRecursive(hostWorkDir);

        LOG.fine(podName + " (Cleaned)");

    }

    static {

        // Create the OpenShift client...
        LOG.info("Creating DefaultOpenShiftClient...");
        client = new DefaultOpenShiftClient();
        LOG.info("Created.");

        // Get the preferred service account...
        OS_SA = IOUtils
                .getConfiguration(SA_ENV_NAME, SA_DEFAULT);
        LOG.info("OS_SA='" + OS_SA + "'");

        // Get the preferred project name...
        OS_PROJECT = IOUtils
                .getConfiguration(PROJECT_ENV_NAME, PROJECT_DEFAULT);
        LOG.info("OS_PROJECT='" + OS_PROJECT + "'");

        // Get the PVC name of the data mount...
        OS_DATA_VOLUME_PVC_NAME = IOUtils
                .getConfiguration(PVC_NAME_ENV_NAME, PVC_NAME_DEFAULT);
        LOG.info("OS_DATA_VOLUME_PVC_NAME='" + OS_DATA_VOLUME_PVC_NAME + "'");

        // And the base-name for all Pods we create...
        OS_POD_BASE_NAME = IOUtils
                .getConfiguration(POD_BASE_NAME_ENV_NAME, POD_BASE_NAME_DEFAULT);
        LOG.info("OS_POD_BASE_NAME='" + OS_POD_BASE_NAME + "'");

        // And the environment setting for all Pods we create...
        OS_POD_ENVIRONMENT = IOUtils
                .getConfiguration(POD_ENVIRONMENT_ENV_NAME, POD_ENVIRONMENT_DEFAULT);
        if (OS_POD_ENVIRONMENT.length() == 0) {
            LOG.info("OS_POD_ENVIRONMENT=(Empty)");
        } else {
            LOG.info("OS_POD_ENVIRONMENT='...'");
        }

        // Get the configured log cpacity
        // (maximum number of lines collected from a Pod).
        // Zero, the default (or -ve values) are interpreted as 'keep all log lines'.
        String logCapacity = IOUtils
                .getConfiguration(LOG_HISTORY_ENV_NAME, LOG_HISTORY_DEFAULT);
        int logHistoryInt = Integer.parseInt(logCapacity);
        if (logHistoryInt < 0) {
            logHistoryInt = 0;
        }
        LOG_HISTORY = logHistoryInt;
        LOG.info("LOG_HISTORY=" + LOG_HISTORY);

    }

}