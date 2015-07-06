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
class SplitAndQueueJobRouteBuilderSpec extends DatasetSpecificationBase {
     
    protected String getTableName() {
         "users_test.users_test_SplitAndQueueJobRouteBuilderSpec"
    }
    
    void doAddRoutes() {
        camelContext.addRoutes(new SplitAndQueueJobRouteBuilder())
    }
    
    void doProcessCamelContext() {
        // setup ActiveMQ
        //def brokerUri = "vm://localhost?broker.persistent=false"
        //def brokerUri = "tcp://localhost:61616";
        //println "ActiveMQ being set up using " + brokerUri
        //camelContext.addComponent(JobServiceRouteBuilder.JMS_BROKER_NAME, activeMQComponent(brokerUri));
    }
	
    
    void "simple 1"() {
        setup:
        def ids = TestUtils.createTestData(getDatasetHandler())
        SplitAndQueueProcessDatasetJobDefinition jobdef = new SplitAndQueueProcessDatasetJobDefinition(
            ids[3],
            "queue1",
            DatasetJobDefinition.DatasetMode.CREATE,
            String.class,
            "new name");

        when:
        JobStatus status1 = producerTemplate.requestBody(SplitAndQueueJobRouteBuilder.ROUTE_SPLIT_AND_QUEUE_SUBMIT, jobdef);
        def job = JobHandler.getJob(camelContext, status1.getJobId())
        
        System.out.println("Status 1: " + status1);
        Thread.sleep(3000);
        JobStatus status2 = job.getCurrentJobStatus()
        System.out.println("Status 2: " + status2);

        then:
        status1.status != null
        status2.totalCount == 5

        
        
    }
    
    
}

