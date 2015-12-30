
import chemaxon.jchem.db.*
import chemaxon.marvin.io.*
import chemaxon.util.ConnectionHandler
import org.squonk.camel.chemaxon.processor.db.AbstractUpdateHandlerProcessor
import org.squonk.camel.processor.ChunkBasedReporter
import org.squonk.chemaxon.molecule.MoleculeUtils
import groovy.sql.Sql
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder

/**
 *
 * @author timbo
 */
class PdbLigandLoader extends AbstractLoader {
   
    static void main(String[] args) {
        println "Running with args: $args"
        def instance = new PdbLigandLoader('pdb_ligand.properties')
        if (args.length > 0) {
            instance.run(args)
        }
    }
    
    PdbLigandLoader(String config) {
        super(new File(config).toURL())
    }
    
    protected void createStructureTableIndexes(Sql db) {
        db.execute "CREATE INDEX idx_${props.table}_pdb_code ON $tableName (pdb_code)".toString()
        db.execute "CREATE INDEX idx_${props.table}_full_code ON $tableName (full_code)".toString()
    }
    
    void executeRoutes(CamelContext camelContext) {
        ProducerTemplate t = camelContext.createProducerTemplate()
        String file = props.path + '/' + props.file
        println "Loading file $file"
        t.sendBody('direct:start', new File(file))
        println "Finished loading"
                              
        Sql db = new Sql(dataSource.getConnection())
        int rows = db.firstRow('select count(*) from ' + database.vendordbs.schema + '.' + props.table)[0]
        println "Table ${props.table} now has $rows rows"
    }
    
    void createRoutes(CamelContext camelContext) {
        final String cols = getColumnNamesFromColumnDefs(props.extraColumnDefs).join(',')
        //println "extracols = $cols"

        Processor updateHandlerProcessor = new AbstractUpdateHandlerProcessor(
            UpdateHandler.INSERT, database.vendordbs.schema + '.' + props.table, 
            cols) {
 
            @Override
            protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
                MRecord rec = exchange.in.getBody(MRecord.class)
                String name = rec.getMoleculeName()
                String pdbCode = name.substring(0, 4).toUpperCase()
                updateHandler.setStructure(rec.getString())
                updateHandler.setValueForAdditionalColumn(1, pdbCode)
                updateHandler.setValueForAdditionalColumn(2, name)
            }
        };


        ConnectionHandler conh = new ConnectionHandler(dataSource.getConnection(), database.vendordbs.schema + '.jchemproperties')
        updateHandlerProcessor.connectionHandler = conh
        
        camelContext.addRoutes(new RouteBuilder() {
                def void configure() {
                    onException()
                    .handled(true)
                    .to('direct:errors')
            
                    from('direct:start')
                    .split().method(MoleculeUtils.class, 'mrecordIterator').streaming()
                    //.log('Processing line ${header.CamelSplitIndex}')
                    //.filter().expression(bean(MoleculeUtils.class, "isNotEmpty"))
                    .filter().groovy("MoleculeUtils.heavyAtomCount(request.body.string) > 5")
                    //.filter().groovy("1 == 2")
                    .process(updateHandlerProcessor)
                    .process(new ChunkBasedReporter(props.reportingChunk))
            
                    from('direct:errors')
                    .log('Error: ${exception.message}')
                    //.log('Error: ${exception.stacktrace}')
                    .transform(body().append('\n'))
                    .to('file:' + props.path + '?fileExist=Append&fileName=' + props.file + '_errors')
                }
            })
    }
	
}

