package org.squonk.dataset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.util.IOUtils;

import java.io.Serializable;

/**
 * Created by timbo on 23/02/17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ThinDescriptor implements Serializable {

    public static final ThinDescriptor DEFAULT_FILTERING_THIN_DESCRIPTOR = new ThinDescriptor("input", "output", true, null, null);

    /** Mandatory name of the input dataset. */
    private final String input;

    /** The name of the output dataset.
     * If null then teh output is not treated as thin and processed verbatum. This allows the input to be made thin but
     * to provides unrelated data in the output.
     */
    private final String output;

    /** Does service execution filter the results.
     * e.g. not all entries in the input are expected in the output.
     * If null then the value is presumed to be false
     */
    private final Boolean filtering;
    /** Are the core details of each entry preserved during execution.
     * e.g. for a Dataset&lt;MoleculeObject&gt; is the structure modified in any way by the service so that it has to be
     * updated in the results.
     * If null then the value is presumed to be true.
     */
    private final Boolean preserve;

    /** Optional set of descriptions of which fields are required by the service for execution.
     * If null then it is assumed that not field values are needed e.g. only the structure and its UUID needs to be sent to
     * the service.
     *
     */
    private final ThinFieldDescriptor[] fieldDescriptors;

    public ThinDescriptor(
            @JsonProperty("input") String input,
            @JsonProperty("output") String output,
            @JsonProperty("filtering") Boolean filtering,
            @JsonProperty("preserve") Boolean preserve,
            @JsonProperty("fieldDescriptors") ThinFieldDescriptor[] fieldDescriptors) {
        this.input = input;
        this.output = output;
        this.filtering = filtering;
        this.preserve = preserve;
        this.fieldDescriptors = fieldDescriptors;
    }

    public ThinDescriptor(String input, String output) {
        this(input, output, null, null, null);
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public Boolean isFiltering() {
        return filtering;
    }

    public Boolean isPreserve() {
        return preserve;
    }

    public ThinFieldDescriptor[] getFieldDescriptors() {
        return fieldDescriptors;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("ThinDescriptor: [");
        b.append("input:").append(input)
                .append(" output:").append(output)
                .append(" filtering:").append(filtering)
                .append(" preserve:").append(preserve)
                .append(" thinFieldDescriptors:[").append(IOUtils.joinArray(fieldDescriptors,",")).append("]");
        b.append("]");
        return b.toString();
    }
}
