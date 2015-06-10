package com.im.lac.service

import com.im.lac.jobs.*
import com.im.lac.jobs.impl.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.*
import org.apache.camel.model.*
import spock.lang.Shared
import spock.lang.Specification
import org.apache.camel.Exchange
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy

/**
 *
 * @author timbo
 */
class ExecutorServiceSpec extends Specification {
    
    @Shared ExecutorService executorService
    @Shared DatasetService datasetService
    @Shared Environment env
    
    void setupSpec() {
        env = Environment.createAndStart(new DatasetService(), new ExecutorService("vm://localhost?broker.persistent=false"))
        executorService = env.executorService
        datasetService = env.datasetService
    }
    	
    void "simple sync update"() {
        setup:
        datasetService.createTestData()
        def item1 = datasetService.get(1l)
        println "Old item: " + item1
        def job =  new SynchronousJob<List<String>>(1l, List.class, ExecutorService.DatasetMode.UPDATE, 'direct:hello')
            
        when:
        JobStatus status = job.execute(env)
        println status
            
        then:
        println "New item: " + datasetService.get(1l)
        datasetService.get(1l)[0] == 'Hello ' + item1[0]
        
    }
        
        
    void "simple sync create"() {
        setup:
        println "simple sync create()"
        datasetService.createTestData()
        def item1 = datasetService.get(1l)
        int count = datasetService.list().size()
        def job =  new SynchronousJob<List<String>>(1l, List.class, ExecutorService.DatasetMode.CREATE, 'direct:hello')
           
        when:
        JobStatus status = job.execute(env)
        println status
        
        then:
        datasetService.list().size() == count + 1
        
    }
    
    void "simple async create"() {
        setup:
        println "simple async create()"
        datasetService.createTestData()
        int count = datasetService.list().size()
        assert count == 3
        def item1 = datasetService.get(1l)
        
        RoutesDefinition routes = new RoutesDefinition()
        RouteDefinition route = routes.route()
        
        String queue = 'async_create_test'
        route.from("activemq:queue:${queue}?disableReplyTo=true")
        .log('received ${body} on ' + queue)
        .delay(100)
        
        executorService.camelContext.addRouteDefinition(route)
        
        def job = new AsynchronousJob<List<String>>(1l, List.class, ExecutorService.DatasetMode.CREATE, queue)
            
        when:
        JobStatus status = job.execute(env)
        long t0 = System.currentTimeMillis()
        while (status != JobStatus.Status.COMPLETED && (System.currentTimeMillis() - t0) < 5000) {
            //println status
            sleep(100)
            status = job.getStatus()
        }    
        
        then:
        println status
        status.status == JobStatus.Status.COMPLETED
        datasetService.list().size() == count + 1
        
        cleanup:
        executorService.camelContext.removeRouteDefinition(route)
        
    }
    
    void "list async create"() {
        setup:
        println "list async create()"
        datasetService.createTestData()
        int count = datasetService.list().size()
        assert count == 3
        def item1 = datasetService.get(1l)
        
        RoutesDefinition routes = new RoutesDefinition()
        RouteDefinition route = routes.route()
        
        String queue = 'async_create_test'
        route.from("activemq:queue:${queue}")
        .process() { Exchange exch ->
            def neu = exch.in.body.collect { 'Hello ' + it}
            exch.in.body = neu            
        }.delay(100)
        
        executorService.camelContext.addRouteDefinition(route)
        
        def job = new AsynchronousStreamingJob<List<String>>(1l, List.class, ExecutorService.DatasetMode.CREATE, queue)
            
        when:
        JobStatus status = job.execute(env)
        long t0 = System.currentTimeMillis()
        while (status != JobStatus.Status.COMPLETED && (System.currentTimeMillis() - t0) < 5000) {
            //println status
            sleep(100)
            status = job.getStatus()
        }    
        
        then:
        println status
        status.status == JobStatus.Status.COMPLETED
        datasetService.list().size() == count + 1
        datasetService.get(4l)[0] == 'Hello ' + item1[0]
        
        cleanup:
        executorService.camelContext.removeRouteDefinition(route)
        
    }
    
    
    void "queue submit"() {
        setup:
        println "queue submit()"
        datasetService.createTestData()
        int datasets = datasetService.list().size()
        assert datasets == 3
        def item1 = datasetService.get(1l)
        def job = new QueueJob<List<String>>(1l, List.class, 'bananas_queue')
        
        
        RoutesDefinition routes = new RoutesDefinition()
        RouteDefinition route = routes.route()
        
        route.from("activemq:queue:bananas_queue?disableReplyTo=true")
        .log('received ${body} on bananas_queue')
        .removeHeader('JMSReplyTo')
        .delay(100)
        .to(ExchangePattern.InOnly, "activemq:queue:" + job.getResponseQueueName());
        
        executorService.camelContext.addRouteDefinition(route)
        
        when:
        JobStatus status = job.execute(env)
        long t0 = System.currentTimeMillis()
        while (status != JobStatus.Status.COMPLETED && (System.currentTimeMillis() - t0) < 5000) {
            //println status
            sleep(100)
            status = job.processResults(env)
        }
        println status
                
        then:
        status.status == JobStatus.Status.COMPLETED
        status.totalCount == 3
        status.processedCount == 3
        status.pendingCount == 0
        datasets + 1 == datasetService.list().size()
        
        cleanup:
        executorService.camelContext.removeRouteDefinition(route)
    
    }
    
    
}

