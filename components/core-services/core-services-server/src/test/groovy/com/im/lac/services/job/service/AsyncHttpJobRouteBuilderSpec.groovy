package com.im.lac.services.job.service

import javax.sql.DataSource
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import spock.lang.Shared
import spock.lang.Specification

import com.im.lac.job.jobdef.*
import com.im.lac.services.*
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
class AsyncHttpJobRouteBuilderSpec extends DatasetSpecificationBase {
    
  
    void doAddRoutes() {
        camelContext.addRoutes(new RestRouteBuilder())
        camelContext.addRoutes(new AsyncHttpJobRouteBuilder())
    }
    
    protected String getTableName() {
         "users.users_test_AsyncHttpJobRouteBuilderSpec"
    }
	
    
    void "simple 1"() {
        setup:
        TestUtils.createTestData(getDatasetHandler())
        AsyncHttpProcessDatasetJobDefinition jobdef = new AsyncHttpProcessDatasetJobDefinition(1l,
            "http://localhost/coreservices/rest/echo",
            DatasetJobDefinition.DatasetMode.CREATE,
            String.class,
                "new name");

        when:
        JobStatus status1 = producerTemplate.requestBody(AsyncHttpJobRouteBuilder.ROUTE_ASYNC_HTTP_SUBMIT, jobdef);
        
        System.out.println("Status 1.1: " + status1);
        //Thread.sleep(3000);
        //def status2 = job1.buildStatus()
        //System.out.println("Status 1.2: " + status2);

        then:
        status1.status != null
        //status2.status == JobStatus.Status.COMPLETED

        
        
    }
    
//     void "simple 5"() {
//        setup:
//        TestUtils.createTestData(getDatasetHandler())
//        AsyncLocalProcessDatasetJobDefinition jobdef = new AsyncLocalProcessDatasetJobDefinition(4l,
//            AsyncLocalJobRouteBuilder.ROUTE_DUMMY,
//            DatasetJobDefinition.DatasetMode.CREATE,
//            String.class,
//                "new name");
//
//        when:
//        JobStatus status1 = producerTemplate.requestBody(AsyncLocalJobRouteBuilder.ROUTE_ASYNC_SUBMIT, jobdef);
//        
//        System.out.println("Status 1.1: " + status1);
//        //Thread.sleep(3000);
//        //def status2 = job1.buildStatus()
//        //System.out.println("Status 1.2: " + status2);
//
//        then:
//        status1.status != null
//        //status2.status == JobStatus.Status.COMPLETED
//
//        
//        
//    }
    
    
}

