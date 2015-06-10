package com.im.lac.service;

/**
 *
 * @author timbo
 */
public class Environment {

    private final DatasetService datasetService;
    private final ExecutorService executorService;

    Environment(DatasetService datasetService, ExecutorService executorService) {
        this.datasetService = datasetService;
        this.executorService = executorService;
    }
    
    public static Environment create(DatasetService datasetService, ExecutorService executorService) {
        return new Environment(datasetService, executorService);
    }
    
    public static Environment createAndStart(DatasetService datasetService, ExecutorService executorService) throws Exception {
        Environment env = Environment.create(datasetService, executorService);
        env.executorService.start();
        return env;
    }

    public DatasetService getDatasetService() {
        return datasetService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
