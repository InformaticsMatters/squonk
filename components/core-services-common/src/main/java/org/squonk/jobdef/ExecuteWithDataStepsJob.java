package org.squonk.jobdef;

import org.squonk.execution.steps.StepDefinition;
import org.squonk.io.IODescriptor;

import java.io.InputStream;

public class ExecuteWithDataStepsJob {

    /** Suggestion for how services can be executed using externally provided data rather that data from a Squonk notebook.
     * This class is not in use - it is just an idea for how this might work.
     *
     * Data would be posted to an endpoint that manages the execution and from that request an instance of this class that
     * fully defines the job is creatd. Data would be posted as multipart/mixed content type with the data (in whatever
     * format) and the job details (the inputs, outputs and steps parameters as JSON) as separate parts.
     *
     * In the typical case there would be one input, one output and one step. In this case there would be two parts to the
     * multipart POST request, the first being the JSON representing the job details and the second being the data that is
     * to be processed (e.g. as Squonk's JSON format or SDF). Given this information Squonk now knows how to execute the
     * request.
     *
     * The response to this POST operation would be a JobStatus containing a job ID that can then be used to monitor the
     * status of the job and to fetch the results. This means we need to store the results on the backend until they are
     * fetched. There would probably need to be an option to specify to delete the results after they have been fetched.
     *
     * Alternatively results could be returned synchronously as the response of the POST operation but this would not work
     * for long running jobs.
     *
     * Its not clear if the inputs and outputs params are needed as it's possible that the types can just be specified as
     * mime types in the HTTP headers.
     *
     * @param data The data to process. Typically there is only a single dataset, but multiple ones are sometimes used.
     * @param inputs Definition of the input data
     * @param outputs Definition of the output data
     * @param steps One of more steps to execute in series. Each one specifies a service to invoke and includes things like
     *              user specified options.
     */
    public ExecuteWithDataStepsJob(
            InputStream[] data,
            IODescriptor[] inputs,
            IODescriptor[] outputs,
            StepDefinition[] steps
    ) {

    }
}
