package com.im.lac.services.util

import groovy.sql.Sql
import java.sql.Connection
import javax.sql.DataSource

import com.im.lac.dataset.DataItem
import com.im.lac.dataset.Metadata
import com.im.lac.services.dataset.service.*

/**
 *
 * @author timbo
 */
class TestUtils {
        
    static List<Long> createTestData(DatasetHandler handler) {

        def ids = []

        ids << handler.createDataset('World', 'test0').id
        ids << handler.createDataset(["one", "two", "three"],'test1').id
        ids << handler.createDataset(["red", "yellow", "green", "blue"],'test2').id
        ids << handler.createDataset(["banana", "pineapple", "orange", "apple", "pear"], 'test3').id
        ids << handler.createDataset(["banana", "pineapple", "orange", "apple", "pear"], 'test3').id
        
        return ids
    }
    
    static DataSource createTestDataSource() {
        
        String pw = System.getenv('LAC_TESTUSER_PASSWORD')
        if (pw == null) {
            throw new IllegalStateException("Environment variable LAC_TESTUSER_PASSWORD not defined")
        }
        return Utils.createDataSource(null, null, 'unittest', "tester", pw)
    }
	
}

