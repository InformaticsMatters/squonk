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
 * A temporary work dir is created under the specified host path. The name is randomly generated and the input and output
 * dirs available once the constructor has been called. Those directories are bound to the container dirs /tmp/input and
 * /tmp/output. Typically you would write any input variables and other content (e.g. a shell script to execute) into
 * the host input dir (obtained from {@link #getInputDir()}}) and then execute the container as appropriate (e.g. execute
 * the shell script you put in the input dir) writing the output to /tmp/output.
 *
 * Then once execution is complete you will find the output in the host dir obtained from {@link #getOutputDir()}}.
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
    private final File baseDir;
    private final File inputDir;
    private final File outputDir;

    /**
     *
     * @param imageName The Docker image to run. Must already be pulled
     * @param hostPathBase The directory on the host where the input and outputs will be written. Must exist and be writeable.
     * @throws IOException
     */
    public DockerRunner(String imageName, String hostPathBase) {
        this.imageName = imageName;
        this.baseDir = new File(hostPathBase + "/" + UUID.randomUUID().toString());
        LOG.fine("Base dir is " + baseDir.getPath());
        this.inputDir = new File(baseDir, "input");
        this.outputDir = new File(baseDir, "output");
    }

    public void init() throws IOException {
        if (!this.baseDir.mkdir()) {
            throw new IOException("Could not create base dir");
        }
        if (!this.inputDir.mkdir()) {
            throw new IOException("Could not create input dir");
        }
        if (!this.outputDir.mkdir()) {
            throw new IOException("Could not create output dir");
        }
    }

    public long writeInput(String filename, InputStream content) throws IOException {
        File file = new File(getInputDir(), filename);
        try {
            return Files.copy(content, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            content.close();
        }
    }

    public long writeInput(String filename, String content) throws IOException {
        return writeInput(filename, new ByteArrayInputStream(content.getBytes()));
    }

    public InputStream readOutput(String filename) throws IOException {
        File f = new File(getOutputDir(), filename);
        if (f.exists()) {
            return new FileInputStream(f);
        } else {
            return null;
        }
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getInputDir() {
        return inputDir;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void clean() {
        deleteRecursive(baseDir);
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

        Volume input = new Volume("/tmp/input");
        Volume output = new Volume("/tmp/output");

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withVolumes(input, output)
                .withBinds(new Bind(inputDir.getPath(), input, AccessMode.ro), new Bind(outputDir.getPath(), output, AccessMode.rw))
                .withCmd(cmd)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        int resp = dockerClient.waitContainerCmd(container.getId()).exec();
        LOG.fine("Docker execution completed. Results written to " + outputDir.getPath());
        return resp;
    }

    public static void main(String[] args) throws IOException {

        DockerRunner runner = new DockerRunner("busybox", "/home/timbo/tmp/work/");
        runner.init();
        runner.writeInput("run.sh", "touch /tmp/output/IWasHere\n");

        long t0 = System.currentTimeMillis();
        runner.execute("/bin/sh", "/tmp/input/run.sh");
        long t1 = System.currentTimeMillis();
        System.out.println("Execution completed in " + (t1-t0) + "ms");
        System.out.println("Results found in " + runner.getOutputDir().getPath());
        runner.clean();
        System.out.println("All data deleted");

    }

}
