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