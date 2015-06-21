package com.im.lac.jobs.impl

import javax.sql.DataSource
import spock.lang.Shared
import spock.lang.Specification
import com.im.lac.service.DatasetService
import groovy.sql.Sql
import java.util.stream.Stream
/**
 *
 * @author timbo
 */
class DatasetHandlerSpec extends Specification {
    
    @Shared DataSource dataSource = com.im.lac.service.Utils.createDataSource()
    @Shared DatasetService service = new DatasetService(dataSource, DatasetService.DEFAULT_TABLE_NAME + "_test_datasethandlerspec")
    @Shared DatasetHandler handler = new DatasetHandler(service, "/tmp/datasetcache")
    
    // run before the first feature method
    def setupSpec() {
        Sql db = new Sql(dataSource)
        try {
            db.execute 'DELETE FROM ' + service.tableName
            db.execute 'DROP TABLE ' + service.tableName
        } catch (Exception e) { }// expected   
        service.createTables()
        List<Long> ids = service.createTestData()
    }     
    
    // run after the last feature method
    def cleanupSpec() {
        Sql db = new Sql(dataSource)
        // first delete so that our LOBs get deleted
        db.execute 'DELETE FROM ' + service.tableName
        db.execute 'DROP TABLE ' + service.tableName
    }   
    
    void "test read large object as text"() {
        
        when:
        Object r = handler.fetchDatasetObjectsForId(1l)
        
        then:
        r instanceof String
        r == 'World'
    }
    
    void "test read large object as stream"() {
        
        when:
        Object r = handler.fetchDatasetObjectsForId(2l)
        
        then:
        r instanceof Stream
        r.count() == 3
    }
	
}

