package com.im.lac.services.job.service.steps

import com.squonk.types.io.JsonHandler
import com.squonk.dataset.DatasetMetadata
import com.im.lac.services.job.variable.MemoryVariableLoader
import com.im.lac.services.job.variable.Variable
import com.im.lac.services.job.variable.VariableManager
import com.im.lac.services.job.service.AsyncJobRouteBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class StepExamplesSpec extends Specification {
	
    
    void "read sdf calculate and cluster"() {
        
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
        
        MemoryVariableLoader loader = new MemoryVariableLoader()
        VariableManager varman = new VariableManager(loader)
        StepExecutor exec = new StepExecutor(varman);
        

        
        // cell 1
        // step 1: read the SDF and generate a Dataset<MoleculeObject>
        SDFReaderStep step1 = new SDFReaderStep()
        FileInputStream is = new FileInputStream("../../../data/testfiles/Kinase_inhibs.sdf.gz")
        Variable sdf = varman.createVariable(
            SDFReaderStep.FIELD_SDF_INPUT, 
            InputStream.class, 
            is,
            Variable.PersistenceType.BYTES)
        step1.configure([:], [(SDFReaderStep.FIELD_DATASET_OUTPUT):'sdf_mols'])
        
        // step 2: write dataset to json
        DatasetWriterStep step2 = new DatasetWriterStep()
        step2.configure([:], [
                (DatasetWriterStep.FIELD_INPUT_DATASET):'sdf_mols',
                (DatasetWriterStep.FIELD_OUTPUT_DATA):'sdf_data_out',
                (DatasetWriterStep.FIELD_OUTPUT_METADATA):'sdf_meta_out'])
        // end of cell 1
        
        
        // cell 2: calculate some props
        // step 3: calculate logPs using CDK. _calc1 is a temp variable
        MoleculeServiceExecutorStep step3 = new MoleculeServiceExecutorStep()
        step3.configure([
                (MoleculeServiceExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-cdk-basic/rest/v1/calculators/logp'
            ], [
                (MoleculeServiceExecutorStep.FIELD_INPUT):'sdf_data_out',
                (MoleculeServiceExecutorStep.FIELD_OUTPUT_DATA):'_calc1',
                (MoleculeServiceExecutorStep.FIELD_OUTPUT_METADATA):'_meta1'
            ])
    
        // step 4: cluster. _calc2 is a temp variable
        MoleculeServiceExecutorStep step4 = new MoleculeServiceExecutorStep()
        step4.configure([
                (MoleculeServiceExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-chemaxon-basic/rest/v1/descriptors/clustering/spherex/ecfp4',
                'header.min_clusters':5,
                'header.max_clusters':10
            ], [
                (MoleculeServiceExecutorStep.FIELD_INPUT):'_calc1',
                (MoleculeServiceExecutorStep.FIELD_OUTPUT_DATA):'_calc2',
                (MoleculeServiceExecutorStep.FIELD_OUTPUT_METADATA):'_meta2'
            ])
        
        // step 4: convert to dataset. we need this to be able to generate the full
        // metadata as the services do not (currently?) provide this.
        DatasetReaderStep step5 = new DatasetReaderStep()
        step5.configure([:], [
                (DatasetReaderStep.FIELD_INPUT_DATA):'_calc2',
                (DatasetReaderStep.FIELD_INPUT_METADATA):'_meta2',
                (DatasetReaderStep.FIELD_OUTPUT_DATASET):'mols_out'])
        
        // step 6: write dataset to json along with the metadata
        DatasetWriterStep step6 = new DatasetWriterStep()
        step6.configure([:], [
                (DatasetWriterStep.FIELD_INPUT_DATASET):'mols_out',
                (DatasetWriterStep.FIELD_OUTPUT_DATA):'data_out',
                (DatasetWriterStep.FIELD_OUTPUT_METADATA):'meta_out'])
        // end of cell 2
        
        

        when:
                 
        exec.execute( [step1, step2] as Step[], context) 
        println "SDF reading complete"
        
        exec.execute( [step3, step4, step5, step6] as Step[], context)
        println "Calculations complete"
        
        
        then:
        def vars = varman.getVariables()
        vars.each {
            println "Var $it.name $it.type $it.persistenceType"
        }
        vars.size() == 5
        
        
        InputStream data = varman.getValue(varman.lookupVariable('data_out'))
        DatasetMetadata meta = varman.getValue(varman.lookupVariable('meta_out'))
        //println "DATA: " + data.text
        println "META: " + JsonHandler.instance.objectToJson(meta)
        
        cleanup:
        context.stop()
    }
    
}

