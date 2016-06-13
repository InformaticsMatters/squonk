package org.squonk.execution.steps.impl

import org.squonk.dataset.Dataset

import org.squonk.execution.variable.VariableManager
import org.squonk.notebook.api.VariableKey
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class SDFReaderStepSpec extends Specification {
    
    void "test read sdf"() {
        VariableManager varman = new VariableManager(null, 1, 1);
        SDFReaderStep step = new SDFReaderStep()
        FileInputStream is = new FileInputStream("../../data/testfiles/Kinase_inhibs.sdf.gz")
        //FileInputStream is = new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz")
        Long producer = 1
        step.configure(producer, "job1", [:],
                [(SDFReaderStep.VAR_SDF_INPUT): new VariableKey(producer, "input")],
                [:])
        varman.putValue(
            new VariableKey(producer,"input"),
            InputStream.class, 
            is)
        
        when:
        step.execute(varman, null)
        Dataset ds = varman.getValue(new VariableKey(producer, SDFReaderStep.VAR_DATASET_OUTPUT), Dataset.class)
        
        then:
        ds != null
        ds.items.size() == 36

        InputStream json = JsonHandler.getInstance().marshalStreamToJsonArray(ds.getStream(), false)
        //println json.text
        //File f = new File("/tmp/dhfr.json")
        //f << json.text

        cleanup:
        is.close()
        
    }
    
	
}

