
import chemaxon.jchem.db.*
import chemaxon.marvin.io.*
import chemaxon.struc.Molecule
import chemaxon.util.ConnectionHandler
import chemaxon.formats.MolImporter
import com.im.lac.chemaxon.molecule.MoleculeUtils
import com.im.lac.camel.processor.ChunkBasedReporter
import com.im.lac.camel.chemaxon.processor.db.DefaultJChemInserter
import groovy.sql.Sql
import java.sql.*
import javax.sql.DataSource
import org.apache.camel.CamelContext
import org.apache.camel.Endpoint
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext

/**
 *
 * @author timbo
 */
class EMoleculesLoader extends AbstractLoader {
   
    static void main(String[] args) {
        println "Running with $args"
        def instance = new EMoleculesLoader('emolecules.properties')
        if (args.length > 0) {
            instance.run(args)
        }
    }
    
    EMoleculesLoader(String config) {
        super(new File(config).toURL())
    }
    
    DefaultJChemInserter createInserter() {
        
        String cols = getColumnNamesFromColumnDefs(props.extraColumnDefs).join(',')
        
        DefaultJChemInserter updateHandlerProcessor = new DefaultJChemInserter(
            database.vendordbs.schema + '.' + props.table, cols, props.fields) {
        

            @Override
            protected void setValues(Exchange exchange, UpdateHandler updateHandler) {
                String data = exchange.in.getBody(String.class)
                String[] vals = data.split(' ')
                updateHandler.structure = dearomatize(vals[0])
                updateHandler.setValueForAdditionalColumn(1, exchange.getContext().getTypeConverter().convertTo(Integer.class, vals[1]))
                updateHandler.setValueForAdditionalColumn(2, exchange.getContext().getTypeConverter().convertTo(Integer.class, vals[2]))  
            }
            
            String dearomatize(String smiles) {
                Molecule mol = MolImporter.importMol(smiles)
                mol.dearomatize()
                String dearom = mol.toFormat("smiles")
                //println dearom
                return dearom
            }
        }

        ConnectionHandler conh = new ConnectionHandler(dataSource.getConnection(), database.vendordbs.schema + '.jchemproperties')
        updateHandlerProcessor.connectionHandler = conh
        
        return updateHandlerProcessor
    }
    
    void executeRoutes(CamelContext camelContext) {
        println "Loading file ${props.path}/${props.file}"
        ProducerTemplate t = camelContext.createProducerTemplate()
        t.sendBody('direct:start', new File(props.path + '/' + props.file))
                                           
        sleep(100)

        Sql db = new Sql(dataSource.getConnection())
        int rows = db.firstRow('select count(*) from ' + database.vendordbs.schema + '.' + props.table)[0]
        println "Found $rows rows"
    }
    
    void createRoutes(CamelContext camelContext) {

        camelContext.addRoutes(new RouteBuilder() {
                
                def void configure() {
                    
                    onException()
                    .handled(true)
                    .to('direct:errors')
                    
                    def numbers = 1..props.processors
                    
                    List<Endpoint> endpoints = []
                    numbers.each {
                        endpoints << endpoint("seda:processor${it}?size=5&blockWhenFull=true")
                    }
                    
                    endpoints.each {
                        from(it)
                        .process(createInserter())
                        .to('seda:report')
                    }
                    
                    
                    from('direct:start')
                    //.unmarshal().gzip()
                    .split(body().tokenize("\n")).streaming()
                    //.log('Input: ${body}')
                    .loadBalance().roundRobin().to(
                        endpoints
                    )      
                 
                    from('seda:report')
                    .process(new ChunkBasedReporter(10000))
                    
                    from('direct:errors')
                    .log('Error: ${body}\nMessage: ${exception.message}')
                    //.log('stacktrace: ${exception.stacktrace}')
                    .transform(body().append('\n'))
                    .to('file:' + props.path + '?fileExist=Append&fileName=' + props.file + '_errors')
                }
            })
    }
	
}

