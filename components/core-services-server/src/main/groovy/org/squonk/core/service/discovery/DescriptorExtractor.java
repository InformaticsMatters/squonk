package org.squonk.core.service.discovery;

import org.squonk.execution.docker.DockerExecutorDescriptor;
import org.squonk.io.DescriptorLoader;
import org.squonk.types.io.JsonHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Created by timbo on 28/12/16.
 */
public class DescriptorExtractor {

    private static final Logger LOG = Logger.getLogger(DescriptorExtractor.class.getName());

    public List<DescriptorLoader<DockerExecutorDescriptor>> loadDockerExecutorDescriptors(URL zipFile) throws IOException {

        JarURLConnection con = (JarURLConnection) zipFile.openConnection();
        JarFile jar = con.getJarFile();
        List<DescriptorLoader<DockerExecutorDescriptor>> results = new ArrayList<>();

        Enumeration<JarEntry> e = jar.entries();
        JsonHandler jsonHandler = JsonHandler.getInstance();
        while (e.hasMoreElements()) {
            JarEntry f = e.nextElement();
            String name = f.getName();
            LOG.fine("Checking " + name);
            if (name.endsWith(".ded") && !f.isDirectory()) {

                try (InputStream is = jar.getInputStream(f)) {
                    DockerExecutorDescriptor ded = jsonHandler.objectFromJson(is, DockerExecutorDescriptor.class);
                    URL url = new URL(zipFile, name);
                    LOG.info("ID: " + ded.getId() + " URL: " + url);
                    DescriptorLoader<DockerExecutorDescriptor> l = new DescriptorLoader<>(url, ded.getId(), DockerExecutorDescriptor.class);
                    results.add(l);
                }
            }
        }

        return results;
    }
}
