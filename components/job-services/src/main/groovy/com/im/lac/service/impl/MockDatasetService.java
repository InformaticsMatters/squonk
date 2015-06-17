package com.im.lac.service.impl;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author timbo
 */
public class MockDatasetService {

    private final AtomicLong currentId = new AtomicLong(0);
    private final Map<Object, Object> datasets = new LinkedHashMap<>();

    /** Re-create the data for testing (deletes any existing datasets). 
     * 
     */
    public void createTestData() {
        datasets.clear();
        currentId.set(0);
        datasets.put(nextId(), Arrays.asList(new String[]{"one", "two", "three"}));
        datasets.put(nextId(), Arrays.asList(new String[]{"red", "yellow", "green", "blue"}));
        datasets.put(nextId(), Arrays.asList(new String[]{"banana", "pineapple", "orange", "apple", "pear"}));
    }

    private long nextId() {
        return currentId.incrementAndGet();
    }

    /**
     * Get a list of all the current datasets
     * 
     * @return 
     */
    public Set<Object> list() {
        return datasets.keySet();
    }

    /**
     * Get the dataset for this ID
     * 
     * @param id
     * @return 
     */
    public Object get(Object id) {
        Object o = datasets.get(id);
        if (o == null) {
            throw new IllegalArgumentException("Dataset with ID " + id + " not found");
        }
        return o;
    }

    /**
     * Store this as a new dataset
     * @param dataset
     * @return The ID of the new dataset
     */
    public Object put(Object dataset) {
        Long id = nextId();
        datasets.put(id, dataset);
        return id;
    }

    /**
     * Update the dataset with this ID
     * 
     * @param id
     * @param dataset 
     */
    public void update(Object id, Object dataset) {
        datasets.put(id, dataset);
    }
    
    /**
     * Delete the dataset with this ID
     * 
     * @param id
     * @return whether the dataset existed and was deleted
     */
    public boolean delete(Object id) {
        return datasets.remove(id) != null;
    }

}
