package com.im.lac.service.impl

import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource
import com.fasterxml.jackson.databind.ObjectMapper

/**
 *
 * @author timbo
 */
class Utils {
    
    private static ObjectMapper mapper = new ObjectMapper()
    
    static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource()
        
        ds.serverName = System.getenv("CHEMCENTRAL_DB_SERVER") ?: 'localhost'
        ds.portNumber =  new Integer(System.getenv("CHEMCENTRAL_DB_PORT") ?: '5432')
        ds.databaseName = System.getenv("CHEMCENTRAL_DB_NAME") ?: 'chemcentral'
        ds.user = System.getenv("CHEMCENTRAL_DB_USERNAME") ?: 'chemcentral'
        ds.password =  System.getenv("CHEMCENTRAL_DB_PASSWORD") ?:  'chemcentral'

        return ds;
    }
    
    static String toJson(Object o) {
        return mapper.writeValueAsString(o)
    }
    
    static <T> T fromJson(InputStream is, Class<T> type) {
        return mapper.readValue(is, type)
    }
    
    static <T> T fromJson(String s, Class<T> type) {
        return mapper.readValue(s, type)
    }
	
}

