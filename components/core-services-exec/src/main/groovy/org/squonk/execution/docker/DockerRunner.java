package org.squonk.execution.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Simple Docker executor that expects inputs and outputs.
 * This uses the docker-java library (https://github.com/docker-java/docker-java).
 *
 * A temporary work dir is created under the specified host path. The name is randomly generated. This directories is bound
 * to the container dirs /tmp/work. Typically you would write any input variables and other content (e.g. a shell script to execute) into
 * the host dir (obtained from {@link #getWorkDir()}}) and then execute the container as appropriate (e.g. execute
 * the shell script you put in the work dir) writing the output to that dir.
 *
 * Then once execution is complete you will find the output in the host dir.
 *
 * Finally, once you are finished with the inputs and outputs you can call the {@link #clean()} method to delete the
 * directories that were created
 *
 *
 * Created by timbo on 30/12/15.
 */
public class DockerRunner {

    private static final Logger LOG = Logger.getLogger(DockerRunner.class.getName());

    private final String imageName;
    private final File workDir;


    /**
     *
     * @param imageName The Docker image to run. Must already be pulled
     * @param hostPathBase The directory on the host where the input and outputs will be written. Must exist and be writeable.
     * @throws IOException
     */
    public DockerRunner(String imageName, String hostPathBase) {
        this.imageName = imageName;
        this.workDir = new File(hostPathBase + "/" + UUID.randomUUID().toString());
        LOG.fine("Work dir is " + workDir.getPath());

    }

    public void init() throws IOException {
        if (!this.workDir.mkdir()) {
            throw new IOException("Could not create work dir");
        }

    }

    public long writeInput(String filename, InputStream content) throws IOException {
        File file = new File(getWorkDir(), filename);
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
        File f = new File(getWorkDir(), filename);
        if (f.exists()) {
            return new FileInputStream(f);
        } else {
            return null;
        }
    }

    public File getWorkDir() {
        return workDir;
    }

    public void clean() {
        deleteRecursive(workDir);
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

    /**
     * Run the container and execute the specified command.
     * This method blocks until execution is complete.
     *
     * @param cmd The command and arguments to execute
     * @return The exit status of the container.
     */
    public int execute(String... cmd) {

        // properties read from $HOME/.docker.io.properties
        DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        Volume work = new Volume("/tmp/work");

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withVolumes(work)
                .withBinds(new Bind(workDir.getPath(), work, AccessMode.rw))
                .withCmd(cmd)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        int resp = dockerClient.waitContainerCmd(container.getId()).exec();
        LOG.fine("Docker execution completed. Results written to " + workDir.getPath());
        return resp;
    }

    public static void main(String[] args) throws IOException {

        DockerRunner runner = new DockerRunner("busybox", "/tmp/work/");
        runner.init();
        runner.writeInput("run.sh", "touch /tmp/work/IWasHere\n");

        long t0 = System.currentTimeMillis();
        runner.execute("/bin/sh", "/tmp/work/run.sh");
        long t1 = System.currentTimeMillis();
        System.out.println("Execution completed in " + (t1-t0) + "ms");
        System.out.println("Results found in " + runner.getWorkDir().getPath());
        runner.clean();
        System.out.println("All data deleted");
    }

}
