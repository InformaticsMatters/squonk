package com.im.lac.services.job.service

import javax.sql.DataSource
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import spock.lang.Shared
import spock.lang.Specification

import com.im.lac.job.jobdef.*
import com.im.lac.services.util.*
import com.im.lac.services.job.*
import com.im.lac.services.job.service.*
import com.im.lac.services.camel.Constants
import com.im.lac.services.dataset.service.*

import groovy.sql.Sql

/**
 *
 * @author timbo
 */
class AsyncJobRouteBuidlerSpec extends Specification {
    
    @Shared DataSource dataSource = Utils.createDataSource()
    @Shared DatasetServiceImpl datasetService
    @Shared CamelContext camelContext
    @Shared ProducerTemplate producerTemplate
    
    void setupSpec() {

        this.datasetService = new DatasetServiceImpl(dataSource,  DatasetServiceImpl.DEFAULT_TABLE_NAME + "_test_AsyncJobRouteBuidlerSpec")
        this.datasetService.createTables()
        
        
        SimpleRegistry registry = new SimpleRegistry()
        registry.put(Constants.DATASET_HANDLER, new DatasetHandler(datasetService, "/tmp/datasetcache"))
        registry.put(Constants.JOB_STORE, new SimpleJobStore())
        camelContext = new DefaultCamelContext(registry)
        camelContext.addRoutes(new AsyncJobRouteBuilder())
        producerTemplate = camelContext.createProducerTemplate()
        camelContext.start()
    }
    	
    def cleanupSpec() {
        camelContext.stop()
        // first delete so that our LOBs get deleted
        datasetService.deleteAllLobs()
        Sql db = new Sql(dataSource.connection)
        db.execute 'DROP TABLE ' + datasetService.tableName
        db.close()
    }   
	
    
    void "simple 1"() {
        setup:
        this.datasetService.createTestData()
        ProcessDatasetJobDefinition jobdef = new ProcessDatasetJobDefinition(1l,
            AsyncJobRouteBuilder.ROUTE_DUMMY,
            DatasetJobDefinition.DatasetMode.CREATE,
            String.class,
                "new name");
        AsynchronousJob job1 = new AsynchronousJob(jobdef);
        camelContext.getRegistry().lookupByNameAndType(Constants.JOB_STORE, JobStore.class).putJob(job1)

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

