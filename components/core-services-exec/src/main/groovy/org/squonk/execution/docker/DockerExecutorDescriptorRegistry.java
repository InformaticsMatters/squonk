package org.squonk.execution.docker;

import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.docker.impl.RDKitDockerExecutorDescriptors;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by timbo on 05/12/16.
 */
public class DockerExecutorDescriptorRegistry {

    private static final Logger LOG = Logger.getLogger(DockerExecutorDescriptorRegistry.class.getName());


    private static DockerExecutorDescriptorRegistry instance;

    private final Map<String,DockerExecutorDescriptor> items = new LinkedHashMap<>();

    public static DockerExecutorDescriptorRegistry getInstance() {
        if (instance == null) {
            instance = new DockerExecutorDescriptorRegistry();
            // TODO - avoid use of singleton
            // TODO - better way to register
            RDKitDockerExecutorDescriptors.registerAll(instance);
        }
        return instance;
    }

    public List<DockerExecutorDescriptor> getDescriptors() {
        List<DockerExecutorDescriptor> list = new ArrayList(items.size());
        list.addAll(items.values());
        return list;
    }

    public DockerExecutorDescriptor fetch(String id) {
        return items.get(id);
    }

    public List<ServiceDescriptor> getServiceDescriptors() {
        List<ServiceDescriptor> list = new ArrayList<>(items.size());
        items.forEach((k,v) -> list.add(v.getServiceDescriptor()));
        return list;
    }


    public void add(DockerExecutorDescriptor descriptor) {
        if (items.containsKey(descriptor.getId())) {
            throw new RuntimeException("Descriptor " + descriptor.getId() + " already registered");
        }
        items.put(descriptor.getId(), descriptor);
        LOG.info("Registered DockerExecutorDescriptor " + descriptor.getId());
    }

    public void remove(String id) {
        items.remove(id);
    }

}
