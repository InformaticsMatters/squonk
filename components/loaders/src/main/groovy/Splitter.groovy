
import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.builder.RouteBuilder
import com.im.lac.chemaxon.molecule.MoleculeUtils


CamelContext camelContext = new DefaultCamelContext()


camelContext.addRoutes(new RouteBuilder() {
        def void configure() {
                    
            from('direct:start')
            .split().method(new MySplitter(), "split").streaming()
            .log('Processing line ${header.CamelSplitIndex} value: ${body}')
            
            
            from('direct:mols')
            .split().method(MoleculeUtils.class, 'mrecordIterator')
            .log('Processing line ${header.CamelSplitIndex}')
        }
    })

camelContext.start()
    
    
ProducerTemplate t = camelContext.createProducerTemplate()

//t.sendBody('direct:start', "0,1,2,3,4,5,6,7,8,9")

t.sendBody('direct:mols', new FileInputStream("/home/timbo/data/structures/drugbank/2015_04/all.sdf"))

sleep(5000)

camelContext.stop()


    