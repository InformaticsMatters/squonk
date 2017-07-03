/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.rdkit.db.loaders

import groovy.sql.Sql
import org.postgresql.ds.PGSimpleDataSource

import javax.sql.DataSource
import java.sql.SQLException

/**
 *
 * @author timbo
 */
class LoaderUtils {
    
    static ConfigObject createConfig(String path) {
        return createConfig(new File(path).toURI().toURL())
    }
    
    static ConfigObject createConfig(URL url) {
        return new ConfigSlurper().parse(url)
    }
    
    static void executeMayFail(Sql db, String desc, String sql) {
        println desc
        try {
            db.execute(sql)
        } catch (SQLException ex) {
            println "Execution failed: ${ex.message}"
        }
    }

    static void execute(Sql db, String desc, String sql) {
        println desc
        db.execute(sql)
    }
    
    static DataSource createDataSource(ConfigObject database, String user, String password) {
        println """Creating datasource:
  server: ${database.server}
  port: ${database.port}
  database: ${database.database}
  user: $user
  password: ******
"""
        PGSimpleDataSource ds = new PGSimpleDataSource()
        ds.serverName = database.server
        ds.portNumber = new Integer(database.port)
        ds.databaseName = database.database
        ds.user = user
        ds.password = password
        
        return ds
    }

}

