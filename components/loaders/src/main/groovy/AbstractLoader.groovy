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

import chemaxon.jchem.db.StructureTableOptions

import javax.sql.DataSource
import org.apache.camel.CamelContext
import org.apache.camel.impl.DefaultCamelContext

import chemaxon.jchem.db.*
import chemaxon.util.ConnectionHandler
import groovy.sql.Sql

/**
 *
 * @author timbo
 */
class AbstractLoader {
    
    ConfigObject database, props
  
    DataSource dataSource
    String tableName
    
    AbstractLoader(URL config) {
        database = LoaderUtils.createConfig('database.properties')
        props = LoaderUtils.createConfig(config)
        validate()
        dataSource = createDataSource()
        tableName = database.vendordbs.schema + '.' + props.table
    }
    
    protected DataSource createDataSource() {     
        return LoaderUtils.createDataSource(database, database.vendordbs.username, database.vendordbs.password)
    }
    
    protected void validate() {
        assert props.schema != null
        assert props.table != null
    }
    
    void run(String[] args) {
        println "Validating ..."
        args.each {
            doAction(it, props)
        }
    }
    
    protected boolean doAction(String action, def props) {
        if (action == 'createTables') {
            createTables(props)
            return true
        } else if (action == 'dropTables') {
            dropTables(props)
            return true
        } else if (action == 'loadData') {
            loadData(props)
            return true
        }
        return false
    }

    protected ConnectionHandler createConnectionHandler() {
        return new ConnectionHandler(dataSource.getConnection(), database.vendordbs.schema + '.jchemproperties')
    }
    
    
    protected void dropTables(def props) {
        println "Dropping table " + tableName
        ConnectionHandler conh = createConnectionHandler()
        if (UpdateHandler.isStructureTable(conh, tableName)) {
            UpdateHandler.dropStructureTable(conh, tableName)
        }
    }
    
    protected void createTables(def props) {
        println "Creating table " + tableName
        String szr = null
        if (props.standardizer) {
            szr = new File(props.standardizer).text
        }
        ConnectionHandler conh = createConnectionHandler()
        if (!DatabaseProperties.propertyTableExists(conh)) {
            DatabaseProperties.createPropertyTable(conh)    
        }

        StructureTableOptions opts = new StructureTableOptions(tableName, props.tableType)
        opts.extraColumnDefinitions = props.extraColumnDefs.join(',')
        opts.standardizerConfig = szr
        UpdateHandler.createStructureTable(conh, opts)
        
        createStructureTableIndexes(new Sql(conh.connection))
    }
    
    protected void createStructureTableIndexes(Sql db) {
        // noop
    }
    
        
    protected void loadData(def props) {
        
        CamelContext context = createCamelContext()
        createRoutes(context)
        context.start()

        executeRoutes(context)

        context.stop()
    }
    
    CamelContext createCamelContext() {
        return new DefaultCamelContext()
    }
    
    List<String> getColumnNamesFromColumnDefs(List defs) {
        def result = []
        defs.each {
            String col = it.trim()
            int pos = col.indexOf(' ')
            result << col.substring(0, pos)
        }
        return result
    }
}