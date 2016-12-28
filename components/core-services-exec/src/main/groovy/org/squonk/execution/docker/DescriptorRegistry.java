package org.squonk.execution.docker;

import org.squonk.core.ServiceDescriptor;
import org.squonk.execution.docker.impl.RDKitDockerExecutorDescriptors;
import org.squonk.io.DescriptorLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 05/12/16.
 */
public class DescriptorRegistry {

    private static final Logger LOG = Logger.getLogger(DescriptorRegistry.class.getName());


    private static DescriptorRegistry instance;

    private final Map<String,DescriptorLoader> items = new LinkedHashMap<>();

    public static DescriptorRegistry getInstance() {
        if (instance == null) {
            instance = new DescriptorRegistry();
            // TODO - avoid use of singleton
            // TODO - better way to register
            //RDKitDockerExecutorDescriptors.registerAll(instance);
        }
        return instance;
    }

    public List<DescriptorLoader> getDescriptorLoaders() {
        List<DescriptorLoader> list = new ArrayList(items.size());
        list.addAll(items.values());
        return list;
    }

    public DescriptorLoader fetch(String id) {
        return items.get(id);
    }

    public void add(String id, DescriptorLoader loader) {
        if (items.containsKey(id)) {
            throw new RuntimeException("Descriptor " + id + " already registered");
        }
        items.put(id, loader);
        LOG.info("Registered DockerExecutorDescriptor " + id);
    }

    public void remove(String id) {
        items.remove(id);
    }

}
