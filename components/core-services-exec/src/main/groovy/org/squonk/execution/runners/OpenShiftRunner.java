/*
 * Copyright (c) 2021 Informatics Matters Ltd.
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
import java.util.*;
import java.util.logging.Logger;

/**
 * An OpenShift-based Docker image executor that expects inputs and outputs.
 * The runner relies on the presence of a Persistent Volume Claim (PVC) that
 * has been successfully bound.
 * <p/>
 * In order to run this within an OpenShift/Kubernetes cluster you will need:
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

    private static final String POD_START_GRACE_PERIOD_M_ENV_NAME = "SQUONK_POD_START_GRACE_PERIOD_M";
    private static final String POD_START_GRACE_PERIOD_M_DEFAULT = "5";

    private static final String POD_DEBUG_MODE_ENV_NAME = "SQUONK_POD_DEBUG_MODE";
    private static final String POD_DEBUG_MODE_DEFAULT = "0";

    private static final String POD_CPU_RESOURCE_ENV_NAME = "SQUONK_POD_CPU_RESOURCE";
    private static final String POD_CPU_RESOURCE_DEFAULT = "1";

    private static final String POD_IMAGE_PULL_POLICY_ENV_NAME = "SQUONK_POD_IMAGE_PULL_POLICY";
    private static final String POD_IMAGE_PULL_POLICY_DEFAULT = "IfNotPresent";

    private static final String POD_NODE_AFFINITY_VALUE_ENV_NAME = "SQUONK_POD_NODE_AFFINITY_VALUE";
    private static final String POD_NODE_AFFINITY_VALUE_DEFAULT = "worker";

    private static final String NF_QUEUE_SIZE_ENV_NAME = "SQUONK_NF_QUEUE_SIZE";
    private static final String NF_QUEUE_SIZE_DEFAULT = "100";

    // The Pod Environment is a string that consists of YAML name:value pairs.
    // If the environment string is present, each name/value pair is injected
    // as a separate environment variable into the Pod container.
    private static final String POD_ENVIRONMENT_ENV_NAME = "SQUONK_POD_ENVIRONMENT";
    private static final String POD_ENVIRONMENT_DEFAULT = "";

    private static final String OS_DATA_VOLUME_PVC_NAME;
    private static final int    OS_NF_QUEUE_SIZE;
    private static final String OS_POD_BASE_NAME;
    private static final String OS_POD_ENVIRONMENT;
    private static final String OS_POD_IMAGE_PULL_POLICY;
    private static final String OS_POD_NODE_AFFINITY_KEY = "informaticsmatters.com/purpose";
    private static final String OS_POD_NODE_AFFINITY_VALUE;
    private static final String OS_POD_NODE_AFFINITY_OPERATOR = "In";
    private static final String OS_POD_RESTART_POLICY = "Never";
    private static final String OS_PROJECT;
    private static final String OS_SA;

    // The OpenShift Job is given a period of time to start.
    // This time accommodates a reasonable time to pull the image
    // from an external repository. We need to do this
    // because I'm not sure, at the moment how to detect pull errors
    // from within OpenShift - so this is 'belt-and-braces' protection.
    // Set via the environment with a default.
    // The value cannot be less than 1 (minute).
    private static final int OS_POD_START_GRACE_PERIOD_M;
    // The Pod debug level (set in the static initialiser).
    // Currently anything other than 0 results in verbose debug.
    private static final int OS_POD_DEBUG_MODE;
    // The Pod CPU resource (request and limit) (set in the static initialiser).
    private static final String OS_POD_CPU_RESOURCE;

    public static final int LOG_HISTORY;

    // Time between checks on the 'job watcher' completion state.
    private static final long WATCHER_POLL_PERIOD_MILLIS = 1000;

    private static OpenShiftClient client;

    // Objects used to watch the Pod's events and its logs
    private Watch watchObject;
    private LogWatch logWatch;
    private LogStream logStream;

    private final String hostBaseWorkDir;
    private final String localWorkDir;
    private String subPath;
    private String podName;
    private String imageName;
    private String imagePullSecret;

    // The nextflow profile name.
    // This is modified if the call to addExtraNextflowConfig() is made.
    // A call to addExtraNextflowConfig indicates that nextflow is running
    // in a OpenShift/Kubernetes environment, and so the profile name
    // would become 'kubernetes'. If it is set the value is placed into
    // the NF_PROFILE_NAME variable of the Container.
    private String nextflowProfileName = "";

    private boolean isExecuting;
    private boolean stopRequested;
    private boolean podCreated;

    private Yaml yaml = new Yaml();

    /**
     * The PodWatcher receives events form the launched Pod
     * and is responsible for determining when the executed Pod has
     * run to completion.
     */
    class PodWatcher implements Watcher<Pod> {

        private String podName;

        // We maintain a number of phases throughout the Pod's lifetime:
        // - "Waiting" (Initial state)
        // - "Starting"
        // - "Running"
        // - "Complete" (Stopped, waiting for the exit code event)
        // - "Finished" (where the exit code is available)
        private String podPhase = "Waiting";
        // The exit code of the Pod's container.
        // Actually assigned when the container has terminated.
        // The default is to assume all is well.
        private int containerExitCode = 0;
        // Has the watcher experienced an 'onClose()' exception? (#ch507).
        // If necessary, the user can act on this to replace the watcher.
        private boolean onCloseException = false;
        private String onCloseExceptionMessage = "";

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
         * Returns true when the watched Pod has passed the 'Waiting' phase
         * (or it's been restarted)
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

        /**
         * Returns true if the watcher received an exception
         * through its onClose() method.
         */
        boolean hasOnCloseException() {
            return onCloseException;
        }

        /**
         * Returns true if the watcher received an exception
         * through its onClose() method.
         */
        String getOnCloseExceptionMessage() {
            return onCloseExceptionMessage;
        }

        @Override
        public void eventReceived(Action action, Pod resource) {

            PodStatus podStatus = resource.getStatus();
            // For debug (it's a large object)...
            if (OS_POD_DEBUG_MODE > 0) {
                LOG.info("podStatus=" + podStatus.toString());
            }

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
                    LOG.info(">>> podCondition=" + podCondition.toString());
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

            // Try to set the phase to 'Running' or 'Complete'...
            if (!podPhase.equals("Complete")) {
                // Iterate through any ContainerStatus objects.
                // If there's a status then it's waiting (not interested in this),
                // running or terminated.
                List<ContainerStatus> containerStatuses = podStatus.getContainerStatuses();
                if (containerStatuses != null) {
                    for (ContainerStatus containerStatus : containerStatuses) {
                        ContainerState cs = containerStatus.getState();
                        LOG.info(">>> CS=" + cs.toString());
                        if (cs != null) {
                            if (cs.getRunning() != null) {
                                LOG.info("Pod is Running");
                                podPhase = "Running";
                            } else if (cs.getTerminated() != null){
                                ContainerStateTerminated csTerm = cs.getTerminated();
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

            // We're actually waiting for...
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

            // If there's an exception, log and keep it.
            // The user may want to re-create the watcher instance
            // as a mechanism of timeout recovery. The watcher can
            // experience an exception like the "too old resource version"
            // if the underlying Pod has been running too long (see ch507).
            //
            // The Pod's only 'Finished' of there's been no exception

            if (cause != null) {

                onCloseException = true;
                onCloseExceptionMessage = cause.getMessage();

                cause.printStackTrace();
                LOG.warning("podName=" + podName +
                            " cause.message=" + onCloseExceptionMessage);

            } else {
                podPhase = "Finished";
            }

        }

    }

    /**
     * The LogStream is used to collect the stdout from the launched Pod.
     * It is created from within the PodWatcher when we're confident the
     * Pod's running.
     */
    class LogStream extends OutputStream {

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
            // If we weren't using the logQueue then the StringBuilder
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
     * @param imagePullSecret If non-null and non-empty, the name of a pull secret,
     *                        expected to exist in the namespace of the Job to be launched,
     *                        that provides credentials to pull the Docker image.
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
    public OpenShiftRunner(String imageName,
                           String imagePullSecret,
                           String hostBaseWorkDir,
                           String localWorkDir,
                           String jobId) {

        super(hostBaseWorkDir, jobId);

        LOG.info("imageName='" + imageName + "'" +
                 " imagePullSecret='" + imagePullSecret + "'" +
                 " hostBaseWorkDir='" + hostBaseWorkDir + "'" +
                 " localWorkDir='" + localWorkDir + "'" +
                 " jobId='" + jobId + "'");

        this.imageName = imageName;
        this.imagePullSecret = imagePullSecret;

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
     * Create a Pod 'watcher' to monitor the Pod execution progress.
     * returning a PodWatcher instance or null on failure.
     *
     * @param podName The name pf the Pod to watch.
     *
     * @throws KubernetesClientException
     */
    private PodWatcher createPodWatcher(String podName) {

        // Close and forget any existing watcher.
        if (watchObject != null) {
            watchObject.close();
            watchObject = null;
        }

        PodWatcher podWatcher = new PodWatcher(podName);
        try {
            watchObject = client.pods()
                    .withName(podName)
                    .watch(podWatcher);
        } catch (KubernetesClientException ex) {

            // Nothing to do other than re-throw...
            LOG.severe("KubernetesClientException creating PodWatcher : " +
                       ex.getMessage());
            watchObject.close();
            watchObject = null;
            podWatcher = null;

        }

       return podWatcher;

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

        // A hint we're going to run a nextflow process.
        // so set the profile name (passed to the POD in an Environment variable)
        // to 'kubernetes'.
        nextflowProfileName = "kubernetes";

        if (originalConfig == null) {
            return null;
        }

        // Add all the stuff Nextflow needs from our OpenShift world...
        String additionalConfig = String.format(
                "executor {\n" +
                "  queueSize = %s\n" +
                "}\n" +
                "k8s {\n" +
                "  storageClaimName = '%s'\n" +
                "  storageMountPath = '%s'\n" +
                "  storageSubPath = '%s'\n" +
                "  serviceAccount = '%s'\n" +
                "  pod = [nodeSelector: '%s']\n" +
                "}\n",
                OS_NF_QUEUE_SIZE,
                OS_DATA_VOLUME_PVC_NAME,
                localWorkDir,
                jobId,
                OS_SA,
                OS_POD_NODE_AFFINITY_KEY + '=' + OS_POD_NODE_AFFINITY_VALUE);

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

        LOG.info("Initialising (hostBaseWorkDir=" + hostBaseWorkDir + ")");

        // Only permitted on initial (created) state
        if (isRunning != RUNNER_CREATED) {
            LOG.warning(podName + " Call to init() when initialised");
        }

        // For now, there's nothing really to initialise.
        // The method is here to comply with protocol.
        // The execute() method creates and prepares the dependent objects.

        LOG.info("Initialised");

        isRunning = RUNNER_INITIALISED;

    }

    /**
     * Executes the container image and blocks until the container
     * (an OpenShift 'Pod') has completed. This method must only be called
     * once, subsequent calls are ignored.
     *
     * The container image is expected to utilise CMD or ENTRYPOINT in order
     * to provide the command to run as the command passed to this method
     * is no longer used.
     * <p/>
     * The runner instance must have been initialised before calling
     * execute() otherwise an error will be immediately returned.
     *
     * @param cmd A command sequence (unused for this runner)
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

        isRunning = RUNNER_RUNNING;
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

        // Container resources
        Map<String,Quantity> limitMap = new HashMap<String,Quantity>();
        limitMap.put("cpu", new Quantity(OS_POD_CPU_RESOURCE));
        Map<String,Quantity> requestMap = new HashMap<String,Quantity>();
        requestMap.put("cpu", new Quantity(OS_POD_CPU_RESOURCE));
        ResourceRequirements resources = new ResourceRequirementsBuilder()
            .withLimits(limitMap)
            .withRequests(requestMap).build();

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

        // Has a profile name been set?
        // If so add a suitable environment variable to pass it to the Pod.
        if (nextflowProfileName.length() > 0) {
            // Set the container's profile (a nextflow feature)
            // via NF_PROFILE_NAME.
            containerEnv.add(new EnvVar("NF_PROFILE_NAME", nextflowProfileName, null));
        }

        // Container (that will run in the Pod).
        //
        // We do not set the command for the container.
        // Instead, we set the working directory to the 'localWorkDir'
        // (typically /squonk/work/docker) where we've writtren
        // an 'execute' scipt). We then rely on the
        // base container's CMD to run './execute'.
        Container podContainer = new ContainerBuilder()
                .withName(podName)
                .withImage(imageName + "-blob")
                .withWorkingDir(localWorkDir)
                .withImagePullPolicy(OS_POD_IMAGE_PULL_POLICY)
                .withEnv(containerEnv)
                .withResources(resources)
                .withVolumeMounts(volumeMount).build();

        // Here we prepare a (potentially empty) list of pull secrets
        // for the Pod. We only expect one secret so the result is
        // either an empty list of a list containing one secret name.
        // The 'imagePullSecret' is oto the secret itself
        // it is simply the name of a pre-deployed Kubernetes Secret object.
        // See https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/
        List<LocalObjectReference> pullSecrets= new ArrayList<>();
        if (imagePullSecret != null && imagePullSecret.length() > 0) {
            pullSecrets.add(new LocalObjectReference(imagePullSecret));
        }

        // Here we add supplemental groups to the Pod.
        // Crucially we need to add our own group as the Pod's
        // supplemental group - so it can share directories
        // and any files we create.
        long gid = new com.sun.security.auth.module.UnixSystem().getGid();
        PodSecurityContext psc = new PodSecurityContextBuilder()
                .withSupplementalGroups(gid).build();

        // Create the Pod's affinity.
        // A Node Affinity that requires the Pod to run
        // on a node designated as a 'worker' (by default).
        LOG.info("nodeAffinity: " + OS_POD_NODE_AFFINITY_KEY +
                    " " + OS_POD_NODE_AFFINITY_OPERATOR +
                    " " + OS_POD_NODE_AFFINITY_VALUE);

// Removed - using affinity knocks other Pods out of the system
//           Using NodeSelector doesn't.

//         // Pods _must_ run on nodes with the supplied purpose ('worker' by default).
//         // The user can change the purpose of the nodes but the affinity
//         // is always "required" when scheduling, not "preferred".
//         // Therefore if your cluster has no suitably labelled nodes
//         // the Pod will not run, instead remaining in a "Pending" state.
//         NodeSelectorRequirement nodeSelectorRequirement = new NodeSelectorRequirementBuilder()
//                 .withKey(OS_POD_NODE_AFFINITY_KEY)
//                 .withOperator(OS_POD_NODE_AFFINITY_OPERATOR)
//                 .withValues(OS_POD_NODE_AFFINITY_VALUE)
//                 .build();
//         NodeSelectorTerm nodeSelectorTerm = new NodeSelectorTermBuilder()
//                 .withMatchExpressions(nodeSelectorRequirement)
//                 .build();
//         NodeSelector nodeSelector = new NodeSelectorBuilder()
//                 .withNodeSelectorTerms(nodeSelectorTerm)
//                 .build();
//         NodeAffinity nodeAffinity = new NodeAffinityBuilder()
//                 .withRequiredDuringSchedulingIgnoredDuringExecution(nodeSelector)
//                 .build();
//         Affinity podAffinity = new AffinityBuilder()
//                 .withNodeAffinity(nodeAffinity)
//                 .build();

        Map<String,String> nodeSelectorMap = new HashMap();
        nodeSelectorMap.put(OS_POD_NODE_AFFINITY_KEY, OS_POD_NODE_AFFINITY_VALUE);

        // The Pod, which runs the container image...
        Pod pod = new PodBuilder()
                .withNewMetadata()
                .withName(podName)
                .withNamespace(OS_PROJECT)
                .endMetadata()
                .withNewSpec()
                .withImagePullSecrets(pullSecrets)
                .withNodeSelector(nodeSelectorMap)
//                .withAffinity(podAffinity)
                .withSecurityContext(psc)
                .withContainers(podContainer)
                .withServiceAccount(OS_SA)
                .withRestartPolicy(OS_POD_RESTART_POLICY)
                .withVolumes(volume)
                .endSpec().build();

        // Create a Watcher to monitor PodEvents
        LOG.info(podName + " (Creating PodWatcher)");
        PodWatcher podWatcher = createPodWatcher(podName);
        if (podWatcher == null) {
            cleanup();
            isRunning = RUNNER_FINISHED;
            throw new IllegalStateException("Exception creating PodWatcher");
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
        boolean podHasStarted = false;
        while (!podStartFailure
               && !podWatcher.podFinished()
               && !stopRequested) {

            // Pause...
            try {
                Thread.sleep(WATCHER_POLL_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Do we need to restart the watcher?
            // i.e. has it encountered an onClose() exception?
            if (podWatcher.hasOnCloseException()) {
                LOG.warning(podName +" PodWatcher has been closed" +
                            " (" + podWatcher.getOnCloseExceptionMessage() + ")");
                LOG.info(podName + " (Re-creating PodWatcher)");
                podWatcher = createPodWatcher(podName);
                if (podWatcher == null) {
                    // Couldn't re-create a PodWatcher -
                    // something's seriously wrong and so we must leave.
                    LOG.severe(podName +" PodWatcher could not be re-created");
                    break;
                }
            }

            // Still waiting for the Pod to 'start'?
            if (!podHasStarted) {
                if (podWatcher.podStarted()) {
                    podHasStarted = true;
                } else {
                    // Have we waited too long for the Pod to start?
                    long now = System.currentTimeMillis();
                    long elapsedMins = (now - podStartTimeMillis) / 60000;
                    if (elapsedMins >= OS_POD_START_GRACE_PERIOD_M) {
                        LOG.warning(podName + " failed to start. Leaving.");
                        podStartFailure = true;
                    }
                }
            }

        }

        // Get the Pod exit code from the PodWatcher (if it exists)
        if (podWatcher != null && !podStartFailure && !stopRequested) {
            containerExitCode = podWatcher.exitCode();
            LOG.info(podName + " containerExitCode=" + containerExitCode);
        }

        LOG.info(podName + " (Log) " + getLog());
        LOG.info(podName + " (Execute complete)");

        // ---

        // Adjust the 'isRunning' state if we terminated without being stopped.
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
        if (isRunning != RUNNER_RUNNING) {
            return;
        }

        isRunning = RUNNER_STOPPING;

        LOG.info(podName + " (Stopping...)");

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

        LOG.info(podName + " (Stopped)");

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

        LOG.info(podName + " (Cleaning up)");

        // Clean up stuff that looks like it needs cleaning up...

        // The Job may have failed to get created.
        if (podCreated) {
            LOG.info(podName + " (deleting Pod...)");
            client.pods()
                    .inNamespace(OS_PROJECT)
                    .withName(podName)
                    .delete();
        }

        // There may not be a PodWatcher, LogWatcher or LogStream
        if (watchObject != null) {
            LOG.info(podName + " (closing watchObject...)");
            watchObject.close();
        }
        if (logWatch != null) {
            LOG.info(podName + " (closing logWatch...)");
            logWatch.close();
        }
        if (logStream != null) {
            LOG.info(podName + " (clearing logStream...)");
            logStream = null;
        }

        super.cleanup();
        deleteRecursive(hostWorkDir);

        LOG.info(podName + " (Cleaned)");

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

        // And the Pod start grace period
        String gracePeriod = IOUtils
                .getConfiguration(POD_START_GRACE_PERIOD_M_ENV_NAME,
                        POD_START_GRACE_PERIOD_M_DEFAULT);
        int gracePeriodInt = Integer.parseInt(gracePeriod);
        if (gracePeriodInt < 1) {
            gracePeriodInt = 1;
        }
        OS_POD_START_GRACE_PERIOD_M = gracePeriodInt;
        LOG.info("OS_POD_START_GRACE_PERIOD_M=" + OS_POD_START_GRACE_PERIOD_M);

        // And the Pod debug (int)
        String podDebug = IOUtils
                .getConfiguration(POD_DEBUG_MODE_ENV_NAME, POD_DEBUG_MODE_DEFAULT);
        int podDebugInt = Integer.parseInt(podDebug);
        OS_POD_DEBUG_MODE = podDebugInt;
        LOG.info("OS_POD_DEBUG=" + OS_POD_DEBUG_MODE);

        // And the Pod resources (expected to define CPU request and limit)
        OS_POD_CPU_RESOURCE = IOUtils
                .getConfiguration(POD_CPU_RESOURCE_ENV_NAME, POD_CPU_RESOURCE_DEFAULT);
        LOG.info("OS_POD_CPU_RESOURCE=" + OS_POD_CPU_RESOURCE);

        // And the Pod Pull Policy
        OS_POD_IMAGE_PULL_POLICY = IOUtils
                .getConfiguration(POD_IMAGE_PULL_POLICY_ENV_NAME, POD_IMAGE_PULL_POLICY_DEFAULT);
        LOG.info("OS_POD_IMAGE_PULL_POLICY=" + OS_POD_IMAGE_PULL_POLICY);

        // And the Node Purpose (for Pod Affinity)
        OS_POD_NODE_AFFINITY_VALUE = IOUtils
                .getConfiguration(POD_NODE_AFFINITY_VALUE_ENV_NAME, POD_NODE_AFFINITY_VALUE_DEFAULT).toLowerCase(Locale.ROOT);
        LOG.info("OS_POD_NODE_AFFINITY_VALUE=" + OS_POD_NODE_AFFINITY_VALUE);

        // And the Nextflow Queue Size (no less than 1)
        String  nfQueueSize = IOUtils
                .getConfiguration(NF_QUEUE_SIZE_ENV_NAME, NF_QUEUE_SIZE_DEFAULT);
        int nfQueueSizeInt = Integer.parseInt(nfQueueSize);
        if (nfQueueSizeInt < 1) {
            nfQueueSizeInt = 1;
        }
        OS_NF_QUEUE_SIZE = nfQueueSizeInt;
        LOG.info("OS_NF_QUEUE_SIZE=" + OS_NF_QUEUE_SIZE);

        // Get the configured log capacity
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
