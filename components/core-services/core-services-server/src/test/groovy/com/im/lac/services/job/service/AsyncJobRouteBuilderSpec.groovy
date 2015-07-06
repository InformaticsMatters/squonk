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
import com.im.lac.services.ServerConstants
import com.im.lac.services.dataset.service.*

import groovy.sql.Sql

/**
 *
 * @author timbo
 */
class AsyncJobRouteBuilderSpec extends DatasetSpecificationBase {
     

    void doAddRoutes() {
        camelContext.addRoutes(new AsyncJobRouteBuilder())
    }
    
    protected String getTableName() {
         "users.users_test_AsyncJobRouteBuilderSpec"
    }
	
    
    void "simple 1"() {
        setup:
        TestUtils.createTestData(getDatasetHandler())
        AsyncProcessDatasetJobDefinition jobdef = new AsyncProcessDatasetJobDefinition(1l,
            AsyncJobRouteBuilder.ROUTE_DUMMY,
            DatasetJobDefinition.DatasetMode.CREATE,
            String.class,
                "new name");

        when:
        JobStatus status1 = producerTemplate.requestBody(AsyncJobRouteBuilder.ROUTE_ASYNC_SUBMIT, jobdef);
        
        System.out.println("Status 1.1: " + status1);
        //Thread.sleep(3000);
        //def status2 = job1.buildStatus()
        //System.out.println("Status 1.2: " + status2);

        then:
        status1.status != null
        //status2.status == JobStatus.Status.COMPLETED

        
        
    }
    
     void "simple 5"() {
        setup:
        TestUtils.createTestData(getDatasetHandler())
        AsyncProcessDatasetJobDefinition jobdef = new AsyncProcessDatasetJobDefinition(4l,
            AsyncJobRouteBuilder.ROUTE_DUMMY,
            DatasetJobDefinition.DatasetMode.CREATE,
            String.class,
                "new name");

        when:
        JobStatus status1 = producerTemplate.requestBody(AsyncJobRouteBuilder.ROUTE_ASYNC_SUBMIT, jobdef);
        
        System.out.println("Status 1.1: " + status1);
        //Thread.sleep(3000);
        //def status2 = job1.buildStatus()
        //System.out.println("Status 1.2: " + status2);

        then:
        status1.status != null
        //status2.status == JobStatus.Status.COMPLETED

        
        
    }
    
    
}

