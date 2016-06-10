package org.squonk.execution.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.AttachContainerResultCallback;
import com.github.dockerjava.core.command.EventsResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Simple Docker executor that expects inputs and outputs.
 * This uses the docker-java library (https://github.com/docker-java/docker-java).
 *
 * A temporary work dir is created under the specified host path. The name is randomly generated. This directories is bound
 * to the container dir specified by the {@link #localWorkDir} property. Typically you would write any input variables and
 * other content (e.g. a shell script to execute) into the host dir (obtained from {@link #getHostWorkDir()}}) and then
 * execute the container as appropriate (e.g. execute the shell script you put in the work dir) writing the output to that dir.
 *
 * Additional volumes and binds can be added prior to execution using the @{link #addVolume} and @{link #addBind} methods
 *
 * Then once execution is complete you will find the output in the host dir.
 *
 * Finally, once you are finished with the inputs and outputs you can call the {@link #cleanWorkDir()} method to delete the
 * directories that were created
 *
 *
 * Created by timbo on 30/12/15.
 */
public class DockerRunner {

    private static final Logger LOG = Logger.getLogger(DockerRunner.class.getName());

    private final String imageName;
    private final String localWorkDir;
    private final File hostWorkDir;

    private final List<Volume> volumes = new ArrayList<>();
    private final List<Bind> binds = new ArrayList<>();
    private LogContainerTestCallback loggingCallback;


    /**
     *
     * @param imageName The Docker image to run. Must already be pulled
     * @param hostBaseWorkDir The directory on the host that will be used to create a work dir. Must exist and be writeable.
     * @param localWorkDir The name under which the host work dir will be mounted in the new container
     * @throws IOException
     */
    public DockerRunner(String imageName, String hostBaseWorkDir, String localWorkDir) {
        this.imageName = imageName;
        this.hostWorkDir = new File(hostBaseWorkDir + "/" + UUID.randomUUID().toString());
        this.localWorkDir = localWorkDir;
        LOG.fine("Host Work dir is " + hostWorkDir.getPath());

    }

    public void init() throws IOException {
        if (!this.hostWorkDir.mkdir()) {
            throw new IOException("Could not create work dir");
        }
        Volume work = new Volume(localWorkDir);
        volumes.add(work);
        Bind b = new Bind(getHostWorkDir().getPath(), work, AccessMode.rw);
        binds.add(b);

    }

    public long writeInput(String filename, InputStream content) throws IOException {
        File file = new File(getHostWorkDir(), filename);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create input file " + filename);
                }
            }
            return Files.copy(content, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            content.close();
        }
    }

    public long writeInput(String filename, String content) throws IOException {
        return writeInput(filename, new ByteArrayInputStream(content.getBytes()));
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

    public void cleanWorkDir() {
        deleteRecursive(hostWorkDir);
    }

    private boolean deleteRecursive(File path) {
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
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

        // properties read from environment variables
        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withVolumes(volumes.toArray(new Volume[volumes.size()]))
                .withBinds(binds.toArray(new Bind[binds.size()]))
                .withCmd(cmd)
                .exec();

        try {
            dockerClient.startContainerCmd(container.getId()).exec();
            LOG.info("Executing command");

            loggingCallback = new LogContainerTestCallback(true);
            dockerClient.logContainerCmd(container.getId())
                    .withStdErr(true)
                    .withStdOut(true)
                    .withFollowStream(true)
                    .withTailAll()
                    .exec(loggingCallback);

            int resp = dockerClient.waitContainerCmd(container.getId()).exec();
            LOG.fine("Docker execution completed. Results written to " + getHostWorkDir().getPath());
            return resp;
        } finally {
            try {
                dockerClient.removeContainerCmd(container.getId()).exec();
            } catch (Exception e) {
                LOG.warning("Failed to removed container " + container.getId());
            }
        }
    }

    public String getLog() {
        return loggingCallback == null ? null : loggingCallback.toString();
    }

    public static void main(String[] args) throws IOException {

        DockerRunner runner = new DockerRunner("busybox", "/tmp/work/", "/source");
        runner.init();
        runner.writeInput("run.sh", "touch /source/IWasHere\n");

        long t0 = System.currentTimeMillis();
        runner.execute("/bin/sh", "/source/run.sh");
        long t1 = System.currentTimeMillis();
        System.out.println("Execution completed in " + (t1-t0) + "ms");
        System.out.println("Results found in " + runner.getHostWorkDir().getPath());
        runner.cleanWorkDir();
        System.out.println("All data deleted");

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
            if(collectFrames) collectedFrames.add(frame);
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
