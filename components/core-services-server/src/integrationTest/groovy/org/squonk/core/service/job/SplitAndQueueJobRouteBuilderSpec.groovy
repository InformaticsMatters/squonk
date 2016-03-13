package org.squonk.core.service.job

import com.im.lac.services.util.*
import com.im.lac.services.job.*
import com.im.lac.services.job.service.*
import com.im.lac.services.*
import com.im.lac.services.dataset.service.*

/**
 *
 * @author timbo
 */
class SplitAndQueueJobRouteBuilderSpec extends DatasetSpecificationBase {
     
    protected String getTableName() {
         "users.users_test_SplitAndQueueJobRouteBuilderSpec"
    }
    
    void doAddRoutes() {
        //camelContext.addRoutes(new SplitAndQueueJobRouteBuilder(System.getenv("RABBITMQ_HOST") ?: "localhost", "/unittest", "tester", TestUtils.LAC_PASSWORD))
    }
	
//    @Ignore
//    void "simple 1"() {
//        setup:
//        def ids = createTestData()
//        SplitAndQueueProcessDatasetJobDefinition jobdef = new SplitAndQueueProcessDatasetJobDefinition(
//            ids[3],
//            "queue1",
//            ProcessDatasetJobDefinition.DatasetMode.CREATE,
//            String.class,
//            "new name");
//
//        when:
//        JobStatus status1 = producerTemplate.requestBodyAndHeader(
//            SplitAndQueueJobRouteBuilder.ROUTE_SPLIT_AND_QUEUE_SUBMIT, jobdef,
//            CommonConstants.HEADER_SQUONK_USERNAME, TestUtils.TEST_USERNAME
//        );
//        def job = JobHandler.getJob(camelContext, status1.getJobId())
//
//        System.out.println("Status 1: " + status1);
//        Thread.sleep(3000);
//        JobStatus status2 = job.getCurrentJobStatus()
//        System.out.println("Status 2: " + status2);
//
//        then:
//        status1.statuses != null
//        status2.totalCount == 5
//
//    }
        
}

