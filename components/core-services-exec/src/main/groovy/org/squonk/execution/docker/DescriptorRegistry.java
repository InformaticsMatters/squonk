package org.squonk.execution.docker;

import org.squonk.io.ExecutableDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 05/12/16.
 */
public class DescriptorRegistry<T extends ExecutableDescriptor> {

    private static final Logger LOG = Logger.getLogger(DescriptorRegistry.class.getName());

    private final Map<String,T> items = new LinkedHashMap<>();

    public List<T> fetchDescriptors() {
        List<T> list = new ArrayList(items.size());
        list.addAll(items.values());
        return list;
    }

    public T fetch(String id) {
        return items.get(id);
    }

    public Map<String,T> fetchAll() {
        return items;
    }

    public void add(String id, T descriptor) {
        if (items.containsKey(id)) {
            throw new RuntimeException("Descriptor " + id + " already registered");
        }
        items.put(id, descriptor);
        LOG.info("Registered ExecutorDescriptor " + id);
    }

    public void remove(String id) {
        items.remove(id);
    }


}
