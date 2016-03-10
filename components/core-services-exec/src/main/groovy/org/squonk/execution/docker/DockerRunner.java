package org.squonk.execution.docker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

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
    public DockerRunner(String imageName, String hostPathBase) throws IOException {
        this.imageName = imageName;
        this.baseDir = new File(hostPathBase + "/" + UUID.randomUUID().toString());
        LOG.fine("Base dir is " + baseDir.getPath());
        this.inputDir = new File(baseDir, "input");
        this.outputDir = new File(baseDir, "output");
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

        File input = runner.getInputDir();
        FileWriter shellScript = new FileWriter(new File(input, "run.sh"));
        shellScript.write("touch /tmp/output/superman\n");
        shellScript.flush();
        shellScript.close();


        runner.execute("/bin/sh", "/tmp/input/run.sh");
        System.out.println("Results found in " + runner.getOutputDir().getPath());

        runner.clean();
        System.out.println("Results cleaned up");

    }

}
