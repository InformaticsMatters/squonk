package com.im.lac.services.job.service.steps

import com.im.lac.services.job.service.AsyncJobRouteBuilder
import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.Dataset
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeServiceFatExecutorStepSpec extends Specification {
    
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
               
        def mols = [
            new MoleculeObject("C", "smiles"),
            new MoleculeObject("CC", "smiles"),
            new MoleculeObject("CCC", "smiles")
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader())
        varman.createVariable(MoleculeServiceFatExecutorStep.FIELD_INPUT_DATASET, Dataset.class, ds, Variable.PersistenceType.NONE)
        
        def options = [
            (MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-cdk-basic/rest/v1/calculators/logp'
        ]
        def mappings = [:]
        
        MoleculeServiceFatExecutorStep step = new MoleculeServiceFatExecutorStep()
        step.configure(options, mappings)
        
        
        when:
        step.execute(varman, context)
        
        then:
        def outvar = varman.lookupVariable(MoleculeServiceFatExecutorStep.FIELD_OUTPUT_DATASET)
        outvar != null
        def output = varman.getValue(outvar)
        output instanceof Dataset
        def items = output.items
        items.size() == 3
        items[0].getValue('CDK_ALogP') != null
        
        cleanup:
        context.stop()
        
        
    }
	
}

