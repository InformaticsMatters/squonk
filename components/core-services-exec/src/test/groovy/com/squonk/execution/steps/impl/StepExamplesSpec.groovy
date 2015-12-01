package com.squonk.execution.steps.impl

import com.squonk.dataset.Dataset
import com.squonk.dataset.DatasetMetadata
import com.squonk.execution.steps.Step
import com.squonk.execution.steps.StepExecutor
import com.squonk.execution.variable.Variable
import com.squonk.execution.variable.VariableManager
import com.squonk.execution.variable.impl.MemoryVariableLoader
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification


/**
 *
 * @author timbo
 */
class StepExamplesSpec extends Specification {
	
    
    DefaultCamelContext createCamelContext() {
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
        return context
    }

//    void "read sdf calculate and cluster"() {
//        println "read sdf calculate and cluster()"
//        DefaultCamelContext context = createCamelContext()
//
//        MemoryVariableLoader loader = new MemoryVariableLoader()
//        VariableManager varman = new VariableManager(loader)
//        StepExecutor exec = new StepExecutor(varman);
//
//        // cell 1
//        // step 1: read the SDF and generate a Dataset<MoleculeObject>
//        SDFReaderStep step1 = new SDFReaderStep()
//        FileInputStream is = new FileInputStream("../../../data/testfiles/Kinase_inhibs.sdf.gz")
//        Variable sdf = varman.createVariable(
//            SDFReaderStep.VAR_SDF_INPUT,
//            InputStream.class,
//            is,
//            Variable.PersistenceType.NONE)
//        step1.configure([:], [(SDFReaderStep.VAR_DATASET_OUTPUT):'sdf_data_out'])
//        // end of cell 1
//
//
//        // cell 2: calculate some props
//        // step a: calculate logPs using CDK. _calc1 is a temp variable
//        MoleculeServiceFatExecutorStep step2a = new MoleculeServiceFatExecutorStep()
//        step2a.configure([
//                (MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-cdk-basic/rest/v1/calculators/logp'
//            ], [
//                (MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET):'sdf_data_out',
//                (MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET):'_calc1'
//            ])
//
//        // step b: cluster
//        MoleculeServiceFatExecutorStep step2b = new MoleculeServiceFatExecutorStep()
//        step2b.configure([
//                (MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-chemaxon-basic/rest/v1/descriptors/clustering/spherex/ecfp4',
//                'header.min_clusters':5,
//                'header.max_clusters':10
//            ], [
//                (MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET):'_calc1',
//                (MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET):'data_out'
//            ])
//        // end of cell 2
//
//
//        when:
//
//        exec.execute( [step1] as Step[], context)
//        println "SDF reading complete"
//        exec.execute( [step2a, step2b] as Step[], context)
//        println "Calculations complete"
//
//
//        then:
//        def vars = varman.getVariables()
//        vars.each {
//            println "Var $it.name $it.type $it.persistenceType"
//        }
//        vars.size() == 2
//
//        Dataset dataset = varman.getValue(varman.lookupVariable('data_out'))
//        DatasetMetadata meta = dataset.getMetadata()
//        dataset.items.size() == 36
//        meta.size == 36
//
//
//        cleanup:
//        context.stop()
//    }


//    void "read sdf calculate and cluster using StepDefinition"() {
//        println "read sdf calculate and cluster using StepDefinition()"
//        DefaultCamelContext context = createCamelContext()
//        
//        MemoryVariableLoader loader = new MemoryVariableLoader()
//        VariableManager varman = new VariableManager(loader)
//        StepExecutor exec = new StepExecutor(varman);
//        
//        // create the initial input
//        FileInputStream is = new FileInputStream("../../../data/testfiles/Kinase_inhibs.sdf.gz")
//        Variable sdf = varman.createVariable(
//            SDFReaderStep.VAR_SDF_INPUT, 
//            InputStream.class, 
//            is,
//            Variable.PersistenceType.NONE)
//        
//        // cell 1
//        // step 1: read the SDF and generate a Dataset<MoleculeObject>
//        StepDefinition step1 = new StepDefinition(
//            STEP_SDF_READER,
//            [:], [(SDFReaderStep.VAR_DATASET_OUTPUT):'sdf_data_out'])
//        // end of cell 1
//        
//        
//        // cell 2: calculate some props
//        // step a: calculate logPs using CDK. _calc1 is a temp variable STEP_MOLECULE_SERVICE_EXECUTOR
//        StepDefinition step2a = new StepDefinition(
//            STEP_MOLECULE_SERVICE_EXECUTOR,
//            [
//                (MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-cdk-basic/rest/v1/calculators/logp'
//            ], [
//                (MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET):'sdf_data_out',
//                (MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET):'_calc1'
//            ])
//    
//        // step b: cluster.
//        StepDefinition step2b = new StepDefinition(
//            STEP_MOLECULE_SERVICE_EXECUTOR,
//            [
//                (MoleculeServiceFatExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-chemaxon-basic/rest/v1/descriptors/clustering/spherex/ecfp4',
//                'header.min_clusters':5,
//                'header.max_clusters':10
//            ], [
//                (MoleculeServiceFatExecutorStep.VAR_INPUT_DATASET):'_calc1',
//                (MoleculeServiceFatExecutorStep.VAR_OUTPUT_DATASET):'data_out'
//            ])
//        // end of cell 2
//        
//        
//
//        when:
//                 
//        exec.execute( [step1] as StepDefinition[], context) 
//        println "SDF reading complete"
//        
//        exec.execute( [step2a, step2b] as StepDefinition[], context)
//        println "Calculations complete"
//        
//        
//        then:
//        def vars = varman.getVariables()
//        vars.each {
//            println "Var $it.name $it.type $it.persistenceType"
//        }
//        vars.size() == 2
//        
//        
//        Dataset dataset = varman.getValue(varman.lookupVariable('data_out'))
//        DatasetMetadata meta = dataset.getMetadata()
//        dataset.items.size() == 36
//        meta.size == 36
//        //println "META: " + JsonHandler.instance.objectToJson(meta)
//        
//        cleanup:
//        context.stop()
//    }
    
}

