package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Not in use as this invovles converting a Dataset<BasicObject> to Dataset</MoleculeObject> and some
 * refactoring will be need to handle this.
 * Constructor made protected until this is resolved.
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ConvertToMoleculeTransform extends AbstractTransform {

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
