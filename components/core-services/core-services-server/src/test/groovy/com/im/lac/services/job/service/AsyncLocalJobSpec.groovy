package com.im.lac.services.job.service

import com.im.lac.job.jobdef.*
import com.im.lac.services.discovery.service.ServiceDescriptorStore
import com.im.lac.services.job.*
import com.im.lac.services.discovery.service.ServiceDiscoveryRouteBuilder
import com.im.lac.services.util.*
import com.im.lac.services.ServerConstants


/**
 *
 * @author timbo
 */
class AsyncLocalJobSpec extends DatasetSpecificationBase {
    
    void doAddRoutes() {
        ServiceDescriptorStore serviceDescriptorStore = new ServiceDescriptorStore()
        serviceDescriptorStore.addServiceDescriptors("ignored", ServiceDiscoveryRouteBuilder.TEST_SERVICE_DESCRIPTORS);
        registry.put(ServerConstants.SERVICE_DESCRIPTOR_STORE, serviceDescriptorStore);         
        camelContext.addRoutes(new ServiceDiscoveryRouteBuilder())
        camelContext.addRoutes(new AsyncJobRouteBuilder());
    }
    
    protected String getTableName() {
         "users.users_test_AsyncLocalJob2Spec"
    }
    
    
    void "simple local 1"() {
        setup:
        def ids = createTestData()
        AsyncLocalProcessDatasetJobDefinition jobdef = new AsyncLocalProcessDatasetJobDefinition(
                "test.echo.local",
                "asyncLocal",
            null, // params
            ids[1], // dataset id
            ProcessDatasetJobDefinition.DatasetMode.CREATE,
            "AsyncLocalJobSpec.simpleLocal")
                
        AsyncLocalJob job = new AsyncLocalJob(jobdef)
    
        when:
        JobStatus status1 = job.start(camelContext, TestUtils.TEST_USERNAME)
            
        println("Status 1.1: " + status1);
        TestUtils.waitForJobToComplete(job, 2500)
        def status2 = job.buildStatus()
        println("Status 1.2: " + status2);
        println "DataItem: " + status2.getResult()
    
        then:
        status1.status == JobStatus.Status.RUNNING
        status2.status == JobStatus.Status.COMPLETED
        status2.result.metadata.size > 0
    }
	
}

