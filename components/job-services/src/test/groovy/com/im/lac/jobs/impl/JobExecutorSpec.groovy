package com.im.lac.jobs.impl

import com.im.lac.jobs.*
import com.im.lac.service.*
import com.im.lac.model.*
import com.im.lac.util.IOUtils;
import groovy.sql.Sql
import javax.sql.DataSource
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.*
import org.apache.camel.model.*
import spock.lang.Shared
import spock.lang.Specification
import org.apache.camel.Exchange
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy
import org.apache.camel.util.IOHelper
import com.fasterxml.jackson.databind.ObjectMapper

/**
 *
 * @author timbo
 */
class JobExecutorSpec extends Specification {
    
    @Shared DataSource dataSource = com.im.lac.service.impl.Utils.createDataSource()
    @Shared CamelExecutor executorService
    @Shared DatasetService datasetService
    @Shared Environment env
    @Shared JobService jobService
    
    void setupSpec() {
        DatasetService datasetService = new DatasetService(dataSource,  DatasetService.DEFAULT_TABLE_NAME + "_test_jobexecutorspec")
        env = Environment.createAndStart(
            datasetService, 
            new CamelExecutor("vm://localhost?broker.persistent=false", datasetService))
        this.executorService = env.executorService
        this.datasetService = env.datasetService
        try {
            datasetService.deleteAllLobs()
            db.execute 'DROP TABLE ' + datasetService.tableName
        } catch (Exception e) { }// expected   
        this.datasetService.createTables()
        this.jobService = new JobService(env)
    }
    
    
    	
    def cleanupSpec() {
        Sql db = new Sql(dataSource.connection)
        // first delete so that our LOBs get deleted
        datasetService.deleteAllLobs()
        db.execute 'DROP TABLE ' + datasetService.tableName
    }   
    
    
//    void "simple sync create"() {
//        setup:
//        List<Long> ids = datasetService.createTestData()
//        def item1 = datasetService.getDataItem(ids[0])
//        def id = ids[0]
//        def jobdef = new ProcessDatasetJobDefinition(
//            id, 
//            "direct:helloToString", 
//            Job.DatasetMode.UPDATE, 
//            String.class, 
//            "a new name")
//        
//        def bananaDI = new DataItem()
//        bananaDI.setName("banana")
//        def fruit = new Fruit(type:'banana', colour:'yellow')
//        def json = toJson(fruit)
//        datasetService.addDataItem(bananaDI, new ByteArrayInputStream(json))
//            
//        when:
//        JobStatus status = jobService.submitProcessDatasetJob(jobdef)
//        println status
//        sleep(2000)
//        String str = datasetService.doInTransactionWithResult(String.class) { sql ->
//            DataItem di = datasetService.getDataItem(sql, id)
//            println "LOID = $di.loid"
//            InputStream is = datasetService.createLargeObjectReader(sql, di.loid)
//            String res = is.text
//            is.close()
//            return res
//        }
//         
//            
//        then:
//        status != null
//        status.jobDefinition != null
//        status.jobDefinition.datasetId == id
//        status.status == JobStatus.Status.RUNNING
//        str == "Hello World"
//        
//    }
    
//    void "simple sync update"() {
//        setup:
//        List<Long> ids = datasetService.createTestData()
//        def item1 = datasetService.getDataItem(ids[0])
//        //println "Old item: " + item1
//        def id = ids[0]
//        def jobdef = new ProcessDatasetJobDefinition(
//            id, 
//            "direct:helloToString", 
//            Job.DatasetMode.UPDATE, 
//            String.class, 
//            "a new name")
//            
//        when:
//        JobStatus status = jobService.submitProcessDatasetJob(jobdef)
//        println status
//        sleep(2000)
//        String str = datasetService.doInTransactionWithResult(String.class) { sql ->
//            DataItem di = datasetService.getDataItem(sql, id)
//            println "LOID = $di.loid"
//            InputStream is = datasetService.createLargeObjectReader(sql, di.loid)
//            String res = is.text
//            is.close()
//            return res
//        }
//         
//            
//        then:
//        status != null
//        status.jobDefinition != null
//        status.jobDefinition.datasetId == id
//        status.status == JobStatus.Status.RUNNING
//        str == "Hello World"
//        
//    }
        
        
//    void "simple sync create"() {
//        setup:
//        println "simple sync create()"
//        datasetService.createTestData()
//        def item1 = datasetService.get(1l)
//        int count = datasetService.list().size()
//        def job =  new SynchronousJob<List<String>>(1l, List.class, Job.DatasetMode.CREATE, 'direct:hello')
//           
//        when:
//        JobStatus status = job.execute(env)
//        println status
//        
//        then:
//        datasetService.list().size() == count + 1
//        
//    }
    
//    void "simple async create"() {
//        setup:
//        println "simple async create()"
//        datasetService.createTestData()
//        int count = datasetService.list().size()
//        assert count == 3
//        def item1 = datasetService.get(1l)
//        
//        RoutesDefinition routes = new RoutesDefinition()
//        RouteDefinition route = routes.route()
//        
//        String queue = 'async_create_test'
//        route.from("activemq:queue:${queue}?disableReplyTo=true")
//        .log('received ${body} on ' + queue)
//        .delay(100)
//        
//        executorService.camelContext.addRouteDefinition(route)
//        
//        def job = new AsynchronousJob<List<String>>(1l, List.class, Job.DatasetMode.CREATE, queue)
//            
//        when:
//        JobStatus status = job.execute(env)
//        long t0 = System.currentTimeMillis()
//        while (status != JobStatus.Status.COMPLETED && (System.currentTimeMillis() - t0) < 5000) {
//            //println status
//            sleep(100)
//            status = job.getStatus()
//        }    
//        
//        then:
//        println status
//        status.status == JobStatus.Status.COMPLETED
//        datasetService.list().size() == count + 1
//        
//        cleanup:
//        executorService.camelContext.removeRouteDefinition(route)
//        
//    }
//    
//    void "list async create"() {
//        setup:
//        println "list async create()"
//        datasetService.createTestData()
//        int count = datasetService.list().size()
//        assert count == 3
//        def item1 = datasetService.get(1l)
//        
//        RoutesDefinition routes = new RoutesDefinition()
//        RouteDefinition route = routes.route()
//        
//        String queue = 'async_create_test'
//        route.from("activemq:queue:${queue}")
//        .process() { Exchange exch ->
//            def neu = exch.in.body.collect { 'Hello ' + it}
//            exch.in.body = neu            
//        }.delay(100)
//        
//        executorService.camelContext.addRouteDefinition(route)
//        
//        def job = new AsynchronousStreamingJob<List<String>>(1l, List.class, Job.DatasetMode.CREATE, queue)
//            
//        when:
//        JobStatus status = job.execute(env)
//        long t0 = System.currentTimeMillis()
//        while (status != JobStatus.Status.COMPLETED && (System.currentTimeMillis() - t0) < 5000) {
//            //println status
//            sleep(100)
//            status = job.getStatus()
//        }    
//        
//        then:
//        println status
//        status.status == JobStatus.Status.COMPLETED
//        datasetService.list().size() == count + 1
//        datasetService.get(4l)[0] == 'Hello ' + item1[0]
//        
//        cleanup:
//        executorService.camelContext.removeRouteDefinition(route)
//        
//    }
//    
//    
//    void "queue submit"() {
//        setup:
//        println "queue submit()"
//        datasetService.createTestData()
//        int datasets = datasetService.list().size()
//        assert datasets == 3
//        def item1 = datasetService.get(1l)
//        def job = new QueueJob<List<String>>(1l, List.class, 'bananas_queue')
//        
//        
//        RoutesDefinition routes = new RoutesDefinition()
//        RouteDefinition route = routes.route()
//        
//        route.from("activemq:queue:bananas_queue?disableReplyTo=true")
//        .log('received ${body} on bananas_queue')
//        .removeHeader('JMSReplyTo')
//        .delay(100)
//        .to(ExchangePattern.InOnly, "activemq:queue:" + job.getResponseQueueName());
//        
//        executorService.camelContext.addRouteDefinition(route)
//        
//        when:
//        JobStatus status = job.execute(env)
//        long t0 = System.currentTimeMillis()
//        while (status != JobStatus.Status.COMPLETED && (System.currentTimeMillis() - t0) < 5000) {
//            //println status
//            sleep(100)
//            status = job.processResults(env)
//        }
//        println status
//                
//        then:
//        status.status == JobStatus.Status.COMPLETED
//        status.totalCount == 3
//        status.processedCount == 3
//        status.pendingCount == 0
//        datasets + 1 == datasetService.list().size()
//        
//        cleanup:
//        executorService.camelContext.removeRouteDefinition(route)
//    
//    }
    
    
}

