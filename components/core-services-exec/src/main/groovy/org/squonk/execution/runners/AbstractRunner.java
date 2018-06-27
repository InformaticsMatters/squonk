package org.squonk.execution.runners;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

/** Base class for runners such as DockerRunner and NextflowRunner that operate on the basis of:
 * <ul>
 *     <li>Write input files and configuration to a tmp work dir</li>
 *     <li>Execute something using those inputs</li>
 *     <li>Read the outputs</li>
 *     <li>Cleanup, including deleting the tmp dir</li>
 * </ul>
 *
 * Created by timbo on 01/08/17.
 */
public abstract class AbstractRunner implements ContainerRunner {

    private static final Logger LOG = Logger.getLogger(AbstractRunner.class.getName());

    protected final File hostWorkDir;
    protected final String jobId;

    static final int RUNNER_CREATED = 0;     // Created, init() to be called
    static final int RUNNER_RUNNNG = 1;      // execute() has been invoked
    static final int RUNNER_FINISHED = 2;    // execute() complete with/without error
    static final int RUNNER_STOPPING = 3;    // stop() has been invoked
    static final int RUNNER_STOPPED = 4;     // stop() has completed
    static final int RUNNER_INITIALISED = 5; // init() has been called

    /**
     * Runner state.
     * See RUNNER_* constants for expected values.
     */
    protected int isRunning = RUNNER_CREATED;

    protected AbstractRunner(String hostBaseWorkDir, String givenJobId) {
        LOG.info("Specified hostBaseWorkDir is " + hostBaseWorkDir);
        if (hostBaseWorkDir == null) {
            hostBaseWorkDir = getDefaultWorkDir();
        }
        LOG.info("Using hostBaseWorkDir of " + hostBaseWorkDir);

        // try to create the base dir if it doesn't already exist (which it really should in prod)
        File f = new File(hostBaseWorkDir);
        if (!f.exists()) {
            if (!f.mkdir()) {
                LOG.warning("Host work dir doesn't exist and couldn't be created - this is not a good sign!");
            }
        }

        // Protect against a missing JobId (we'll assign our own).
        // Also protects old behaviour where jobId was auto-assigned.
        if (givenJobId == null) {
            givenJobId = UUID.randomUUID().toString();
            LOG.warning("Missing givenJobId. Using new UUID '" + givenJobId + "'");
        }
        this.jobId = givenJobId;
        this.hostWorkDir = new File(hostBaseWorkDir + "/" + this.jobId);
        LOG.info("Host Work dir is " + hostWorkDir.getPath());

    }

    public void init() throws IOException {
        LOG.info("init() - " + hostWorkDir.getPath());
        // The working directory cannot exist.
        // if it does this may indicate the clean-up process failed for
        // an earlier run or init()'s been called twice..
        if (hostWorkDir.exists()) {
            throw new IOException("Work dir exists" +
                    " (" + hostWorkDir.getPath() + ")." +
                    " Clean-up problem or double-init()?");
        }
        if (!hostWorkDir.mkdir()) {
            throw new IOException("Could not create work dir " + hostWorkDir.getPath());
        }
        // set writable by any user - we don't know which user the container will run as
        hostWorkDir.setWritable(true, false);
    }

    protected abstract String getDefaultWorkDir();

    public abstract void stop();


    public File getHostWorkDir() {
        return hostWorkDir;
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

    public void cleanup() {
        deleteRecursive(hostWorkDir);
    }

    protected boolean deleteRecursive(File path) {
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    public boolean isRunning() {
        return isRunning == 1;
    }

    protected int getCurrentStatus() {
        return isRunning;
    }

    /** Generate a Properties file object from this file name.
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public Properties getFileAsProperties(String filename) throws IOException {
        try (InputStream is = readOutput(filename)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props;
            }
        }
        return null;
    }


}
