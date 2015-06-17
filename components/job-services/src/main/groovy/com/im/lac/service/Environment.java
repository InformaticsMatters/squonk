package com.im.lac.service;

import com.im.lac.jobs.impl.CamelExecutor;


/**
 *
 * @author timbo
 */
public class Environment {

    private final DatasetService datasetService;
    private final CamelExecutor executorService;

    Environment(DatasetService datasetService, CamelExecutor executorService) {
        this.datasetService = datasetService;
        this.executorService = executorService;
    }
    
    public static Environment create(DatasetService datasetService, CamelExecutor executorService) {
        return new Environment(datasetService, executorService);
    }
    
    public static Environment createAndStart(DatasetService datasetService, CamelExecutor executorService) throws Exception {
        Environment env = Environment.create(datasetService, executorService);
        env.executorService.start();
        return env;
    }

    public DatasetService getDatasetService() {
        return datasetService;
    }

    public CamelExecutor getExecutorService() {
        return executorService;
    }
}
