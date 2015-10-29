package com.im.lac.services.job.service.steps

import com.im.lac.services.job.service.AsyncJobRouteBuilder
import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeServiceExecutorSpec extends Specification {
    
    void "test simple service"() {
        
        DefaultCamelContext context = new DefaultCamelContext()
        RouteBuilder rb = new RouteBuilder() {
            public void configure() {
                from(AsyncJobRouteBuilder.ROUTE_HTTP_SUBMIT)
                .log("ROUTE_HTTP_SUBMIT received")
                .log('Routing to ${header[' + Exchange.HTTP_URI + ']}')
                .to("http4:dummy")
                .log("HTTP response received");
            }
        }
        context.addRoutes(rb)
        context.start()
        
        def molsdata = '[{"uuid":"b36c7d87-8418-4f3e-a354-65ae166c5210","source":"C","format":"smiles"},{"uuid":"4c7e6801-0af5-4292-af45-bc7a4c6bdc21","source":"CC","format":"smiles"},{"uuid":"3c37309f-15d4-453c-afee-905a8e82ea4d","source":"CCC","format":"smiles"}]'
        def molsvar = new Variable(MoleculeServiceExecutorStep.FIELD_INPUT, InputStream.class, Variable.PersistenceType.BYTES)
               
        VariableManager varman = new VariableManager(new MemoryVariableLoader([
                    (molsvar): molsdata.bytes
                ]));
        
        def options = [
            (MoleculeServiceExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-cdk-basic/rest/v1/calculators/logp'
        ]
        def mappings = [:]
        
        MoleculeServiceExecutorStep step = new MoleculeServiceExecutorStep()
        step.configure(options, mappings)
        
        
        when:
        step.execute(varman, context)
        
        then:
        def outvar = varman.lookupVariable(MoleculeServiceExecutorStep.FIELD_OUTPUT_DATA)
        outvar != null
        def output = varman.getValue(outvar)
        output instanceof InputStream
        def txt = output.text
        txt.contains('CDK_ALogP')
        //println "TXT: $txt"
        
        cleanup:
        context.stop()
        
        
    }
	
}

