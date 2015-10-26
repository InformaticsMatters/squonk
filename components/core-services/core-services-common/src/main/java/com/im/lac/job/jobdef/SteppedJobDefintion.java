package com.im.lac.job.jobdef;

import com.im.lac.job.step.RESTInvocationConsumingStepDefinition;
import com.im.lac.job.step.ReadResourceStepDefinition;
import com.im.lac.job.step.StepDefinition;
import com.im.lac.job.step.WriteResourceStepDefinition;
import java.net.URI;

/**
 *
 * @author timbo
 */
public class SteppedJobDefintion implements JobDefinition {

    private StepDefinition[] steps;

    public StepDefinition[] getSteps() {
        return steps;
    }

    public void configure(StepDefinition[] steps) {
        this.steps = steps;
    }

    public static void main(String[] args) {
        // simple example of reading data, processing it and writing the result, 
        // assuming no parameters or variable renaming is needed
        StepDefinition[] steps = new StepDefinition[]{
            new ReadResourceStepDefinition(URI.create("http://urlofinputvariable/foo"), null), // assume no params
            new RESTInvocationConsumingStepDefinition(URI.create("http://urlofservice/bar"), null), // assume no params
            new WriteResourceStepDefinition(URI.create("http://urlofoutputvariable/bar"), null) // assume no params
        };

        SteppedJobDefintion jobDef = new SteppedJobDefintion();
        jobDef.configure(steps);
        // send for execution

    }

}
