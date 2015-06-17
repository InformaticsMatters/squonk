package com.im.lac.service

import javax.sql.DataSource
import org.postgresql.ds.PGSimpleDataSource

/**
 *
 * @author timbo
 */
class Utils {
    
    static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource()
        
        ds.serverName = System.getenv("CHEMCENTRAL_DB_SERVER") ?: 'localhost'
        ds.portNumber =  new Integer(System.getenv("CHEMCENTRAL_DB_PORT") ?: '5432')
        ds.databaseName = System.getenv("CHEMCENTRAL_DB_NAME") ?: 'chemcentral'
        ds.user = System.getenv("CHEMCENTRAL_DB_USERNAME") ?: 'chemcentral'
        ds.password =  System.getenv("CHEMCENTRAL_DB_PASSWORD") ?:  'chemcentral'

        return ds;
    }
	
}

