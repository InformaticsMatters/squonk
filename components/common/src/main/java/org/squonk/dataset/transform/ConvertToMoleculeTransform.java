package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Convert the Dataset to a Dataset<MoleculeObject> using the specified field and format
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConvertToMoleculeTransform extends AbstractTransform {

    private final String structureFieldName;
    private final String structureFormat;

    protected ConvertToMoleculeTransform(
            @JsonProperty("structureFieldName")String structureFieldName,
            @JsonProperty("structureFormat")String structureFormat) {
        this.structureFieldName = structureFieldName;
        this.structureFormat = structureFormat;
    }

    public String getStructureFieldName() {
        return structureFieldName;
    }

    public String getStructureFormat() {
        return structureFormat;
    }

}
