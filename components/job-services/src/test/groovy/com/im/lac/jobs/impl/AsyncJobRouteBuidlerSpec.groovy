package com.im.lac.jobs.impl

import javax.sql.DataSource
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import spock.lang.Shared
import spock.lang.Specification

import com.im.lac.model.ProcessDatasetJobDefinition
import com.im.lac.service.DatasetService
import com.im.lac.service.impl.SimpleJobStore
import com.im.lac.jobs.*
import com.im.lac.service.JobStore
import groovy.sql.Sql

/**
 *
 * @author timbo
 */
class AsyncJobRouteBuidlerSpec extends Specification {
    
    @Shared DataSource dataSource = com.im.lac.service.Utils.createDataSource()
    @Shared DatasetService datasetService
    @Shared CamelContext camelContext
    @Shared ProducerTemplate producerTemplate
    
    void setupSpec() {

        this.datasetService = new DatasetService(dataSource,  DatasetService.DEFAULT_TABLE_NAME + "_test_AsyncJobRouteBuidlerSpec")
        this.datasetService.createTables()
        
        
        SimpleRegistry registry = new SimpleRegistry()
        registry.put(CamelExecutor.DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"))
        registry.put(CamelExecutor.JOB_STORE, new SimpleJobStore())
        camelContext = new DefaultCamelContext(registry)
        camelContext.addRoutes(new AsyncJobRouteBuilder())
        producerTemplate = camelContext.createProducerTemplate()
        camelContext.start()
    }
    	
    def cleanupSpec() {
        camelContext.stop()
        // first delete so that our LOBs get deleted
        Sql db = new Sql(dataSource.connection)
        db.execute 'DELETE FROM ' + datasetService.tableName
        db.execute 'DROP TABLE ' + datasetService.tableName
    }   
	
    
    void "simple 1"() {
        setup:
        this.datasetService.createTestData()
        ProcessDatasetJobDefinition jobdef = new ProcessDatasetJobDefinition(1l,
            AsyncJobRouteBuilder.ROUTE_DUMMY,
            Job.DatasetMode.CREATE,
            String.class,
                "new name");
        AsynchronousJob job1 = new AsynchronousJob(jobdef);
        camelContext.getRegistry().lookupByNameAndType(CamelExecutor.JOB_STORE, JobStore.class).putJob(job1)

        when:
        def status1 = producerTemplate.requestBody(AsyncJobRouteBuilder.ROUTE_SUBMIT, job1);
        
        System.out.println("Status 1.1: " + status1);
        Thread.sleep(3000);
        def status2 = job1.buildStatus()
        System.out.println("Status 1.2: " + status2);

        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED

        
        
    }
    
    
}

