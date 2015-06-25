package com.im.lac.jobs.impl;

import com.im.lac.service.DatasetService;
import com.im.lac.service.Utils;

/**
 *
 * @author timbo
 */
public class Main {

    public static void main(String[] args) throws Exception {
        DatasetService service = new DatasetService(Utils.createDataSource());
        service.createTables();
        CamelExecutor executor = new CamelExecutor("vm://localhost?broker.persistent=false", service);
        executor.start();
        System.out.println("Services running");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Stopping services");
                try {
                    executor.stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Thread.currentThread().join();
    }

}
