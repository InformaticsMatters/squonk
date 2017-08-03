package org.squonk.execution.runners;

import org.squonk.execution.util.GroovyUtils;
import org.squonk.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
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
public abstract class AbstractRunner {

    private static final Logger LOG = Logger.getLogger(AbstractRunner.class.getName());

    protected final File hostWorkDir;

    /**
     * 0 = not started
     * 1 = running
     * 2 = finished
     */
    protected int isRunning = 0;

    protected AbstractRunner(String hostBaseWorkDir) {
        LOG.info("Using hostBaseWorkDir of " + hostBaseWorkDir);
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

        this.hostWorkDir = new File(hostBaseWorkDir + "/" + UUID.randomUUID().toString());
        LOG.info("Host Work dir is " + hostWorkDir.getPath());
    }

    public void init() throws IOException {
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
