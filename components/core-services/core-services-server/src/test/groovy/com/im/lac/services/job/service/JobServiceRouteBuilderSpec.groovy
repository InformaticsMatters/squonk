package com.im.lac.services.job.service

import com.im.lac.services.camel.CamelLifeCycle
import com.im.lac.job.jobdef.*
import com.im.lac.services.*
import com.im.lac.services.job.service.*
import com.im.lac.services.dataset.service.*
import com.im.lac.services.util.*
import groovy.sql.Sql
import java.util.concurrent.ExecutorService
import javax.activation.DataSource
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry
import spock.lang.Shared
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class JobServiceRouteBuilderSpec extends DatasetSpecificationBase {
    
    
    void doAddRoutes() {
        camelContext.addRoutes(new AsyncJobRouteBuilder())
        camelContext.addRoutes(new JobServiceRouteBuilder())
    }
    
    protected String getTableName() {
         "users.users_test_JobServiceRouteBuilderSpec"
    }

        
    void "test submit ProcessDatasetJobDefinition"() {
        setup:
        def ids = TestUtils.createTestData(getDatasetHandler())
            
        when:
        def result = producerTemplate.requestBody(
            JobServiceRouteBuilder.ROUTE_SUBMIT_JOB, 
            new AsyncLocalProcessDatasetJobDefinition(
                "test.echo.local",
                "asyncLocal",
                null, // params
                ids[0], // dataset id
                ProcessDatasetJobDefinition.DatasetMode.CREATE,
                "new name")
        );
        println "Result: " + result
        sleep(2000)
        def jobs = JobHandler.getJobStore(camelContext).getJobs()
        def dataItems = DatasetHandler.getDatasetHandler(camelContext).listDataItems()
    
        then:
        result != null
        jobs.size() == 1
        jobs[0].status == JobStatus.Status.COMPLETED
        ids.size == dataItems.size() -1
            
    }
	
}

