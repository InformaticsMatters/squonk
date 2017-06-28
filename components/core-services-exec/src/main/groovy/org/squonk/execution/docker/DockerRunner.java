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

package org.squonk.execution.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import org.squonk.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Docker executor that expects inputs and outputs.
 * This uses the docker-java library (https://github.com/docker-java/docker-java).
 * <p>
 * A temporary work dir is created under the specified host path. The name is randomly generated. This directories is bound
 * to the container dir specified by the {@link #localWorkDir} property. Typically you would write any input variables and
 * other content (e.g. a shell script to execute) into the host dir (obtained from {@link #getHostWorkDir()}}) and then
 * execute the container as appropriate (e.g. execute the shell script you put in the work dir) writing the output to that dir.
 * <p>
 * Additional volumes and binds can be added prior to execution using the @{link #addVolume} and @{link #addBind} methods
 * <p>
 * Then once execution is complete you will find the output in the host dir.
 * <p>
 * Finally, once you are finished with the inputs and outputs you should call the {@link #cleanup()} method to delete the
 * directories that were created
 * <p>
 * <p>
 * Created by timbo on 30/12/15.
 */
public class DockerRunner {

    private static final Logger LOG = Logger.getLogger(DockerRunner.class.getName());

    private final String imageName;
    private final String localWorkDir;
    private final File hostWorkDir;
    private String networkName = "none";

    private final List<Volume> volumes = new ArrayList<>();
    private final List<Bind> binds = new ArrayList<>();
    private LogContainerTestCallback loggingCallback;

    private DockerClientConfig config;
    private DockerClient dockerClient;
    private String containerId;
    /**
     * 0 = not started
     * 1 = running
     * 2 = finished
     */
    private int isRunning = 0;


    /**
     * @param imageName       The Docker image to run. Must already be pulled
     * @param hostBaseWorkDir The directory on the host that will be used to create a work dir. Must exist or be creatable and be writeable.
     * @param localWorkDir    The name under which the host work dir will be mounted in the new container
     */
    public DockerRunner(String imageName, String hostBaseWorkDir, String localWorkDir) {

        LOG.info("Using hostBaseWorkDir of " + hostBaseWorkDir);
        if (hostBaseWorkDir == null) {
            hostBaseWorkDir = IOUtils.getConfiguration("SQUONK_DOCKER_WORK_DIR", "/tmp/work/test");
        }
        LOG.info("Using hostBaseWorkDir of " + hostBaseWorkDir);

        // try to create the base dir if it doesn't already exist (which it really should in prod)
        File f = new File(hostBaseWorkDir);
        if (!f.exists()) {
            if (!f.mkdir()) {
                LOG.warning("Host work dir doesn't exist and couldn't be created - this is not a good sign!");
            }
        }
        this.imageName = imageName;
        this.hostWorkDir = new File(hostBaseWorkDir + "/" + UUID.randomUUID().toString());
        this.localWorkDir = localWorkDir;
        LOG.info("Host Work dir is " + hostWorkDir.getPath());
    }

    protected DockerClient getDockerClient() {
        return dockerClient;
    }

    protected String getContainerId() {
        return containerId;
    }

    public DockerRunner withNetwork(String networkName) {
        this.networkName = networkName;
        return this;
    }

    public void init() throws IOException {
        if (!hostWorkDir.mkdir()) {
            throw new IOException("Could not create work dir " + hostWorkDir.getPath());
        }
        // set writable by any user - we don't know which user the container will run as
        hostWorkDir.setWritable(true, false);
        Volume work = new Volume(localWorkDir);
        volumes.add(work);
        Bind b = new Bind(getHostWorkDir().getPath(), work, AccessMode.rw);
        binds.add(b);

        // properties read from environment variables
        config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
    }

    public long writeInput(String filename, InputStream content, boolean executable) throws IOException {

        File file = new File(getHostWorkDir(), filename);
        LOG.info("Writing to file " + file.getPath());
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create input file " + filename);
                }
            }
            long bytes = Files.copy(content, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            file.setExecutable(executable);
            return bytes;
        } finally {
            content.close();
        }
    }

    public long writeInput(String filename, InputStream content) throws IOException {
        return writeInput(filename, content, false);
    }


    public long writeInput(String filename, String content, boolean executable) throws IOException {
        return writeInput(filename, new ByteArrayInputStream(content.getBytes()), executable);
    }

    public long writeInput(String filename, String content) throws IOException {
        return writeInput(filename, content, false);
    }

    public InputStream readOutput(String filename) throws IOException {
        File f = new File(getHostWorkDir(), filename);
        if (f.exists()) {
            return new FileInputStream(f);
        } else {
            return null;
        }
    }

    public File getHostWorkDir() {
        return hostWorkDir;
    }

    public String getLocalWorkDir() {
        return localWorkDir;
    }

    public void cleanup() {
        if (dockerClient != null) {
            if (containerId != null) {
                try {
                    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to remove container", e);
                }
            }
            IOUtils.close(dockerClient);
        }

        deleteRecursive(hostWorkDir);
    }

    private boolean deleteRecursive(File path) {
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    public Volume addVolume(String localDir) {
        Volume v = new Volume(localDir);
        volumes.add(v);
        return v;
    }

    public Bind addBind(String hostDir, Volume volume, AccessMode mode) {
        Bind b = new Bind(hostDir, volume, mode);
        binds.add(b);
        return b;
    }

    /**
     * Run the container and execute the specified command.
     * This method blocks until execution is complete.
     *
     * @param cmd The command and arguments to execute
     * @return The exit status of the container.
     */
    public int execute(String... cmd) {

        if (isRunning != 0) {
            throw new IllegalStateException("Already started");
        }
        isRunning = 1;

        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(imageName)
                .withVolumes(volumes.toArray(new Volume[volumes.size()]))
                .withBinds(binds.toArray(new Bind[binds.size()]))
                .withWorkingDir(localWorkDir)
                .withNetworkMode(networkName == null ? "none" : networkName)
                .withCmd(cmd);

        CreateContainerResponse container = createContainerCmd.exec();
        containerId = container.getId();
        LOG.info("Created container " + containerId);

        LOG.info("Executing command: " + Arrays.deepToString(cmd));
        dockerClient.startContainerCmd(containerId).exec();

        loggingCallback = new LogContainerTestCallback(true);
        dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .withTailAll()
                .exec(loggingCallback);

        int resp = dockerClient.waitContainerCmd(containerId).exec(new WaitContainerResultCallback()).awaitStatusCode();
        LOG.info("Docker execution completed. Results written to " + getHostWorkDir().getPath());
        return resp;
    }

    public void stop() {
        if (isRunning != 1 || containerId == null) {
            // not started or already finished
            LOG.info("Can't stop container that is not running. Current status is " + isRunning + " container ID is " + containerId);
        } else {
            LOG.info("Stopping container " + containerId);
            DockerClient client = DockerClientBuilder.getInstance(config).build();
            client.stopContainerCmd(containerId).exec();
            LOG.info("Container " + containerId + " stopped");
            isRunning = 2;
        }
    }

    public InspectContainerResponse inspectContainer() {
        if (containerId == null) {
            return null;
        } else {
            DockerClient client = DockerClientBuilder.getInstance(config).build();
            InspectContainerResponse resp = client.inspectContainerCmd(containerId).exec();
            IOUtils.close(client);
            return resp;
        }
    }

    public boolean isRunning() {
        return isRunning == 1;
    }

    protected int getCurrentStatus() {
        return isRunning;
    }

    public String getLog() {
        return loggingCallback == null ? null : loggingCallback.toString();
    }


    public static class LogContainerTestCallback extends LogContainerResultCallback {
        protected final StringBuffer log = new StringBuffer();

        List<Frame> collectedFrames = new ArrayList<Frame>();

        boolean collectFrames = false;

        public LogContainerTestCallback() {
            this(false);
        }

        public LogContainerTestCallback(boolean collectFrames) {
            this.collectFrames = collectFrames;
        }

        @Override
        public void onNext(Frame frame) {
            if (collectFrames) collectedFrames.add(frame);
            log.append(new String(frame.getPayload()));
        }

        @Override
        public String toString() {
            return log.toString();
        }


        public List<Frame> getCollectedFrames() {
            return collectedFrames;
        }
    }

}
