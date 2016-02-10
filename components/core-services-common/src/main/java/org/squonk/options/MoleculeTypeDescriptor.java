package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.types.Structure;

/**
 * Created by timbo on 03/02/16.
 */
public class MoleculeTypeDescriptor <T> extends SimpleTypeDescriptor<Structure> {

    public enum MoleculeType {DISCRETE, QUERY, REACTION}

    private final MoleculeType molType;

    public MoleculeTypeDescriptor(@JsonProperty("molType") MoleculeType molType) {
        super(Structure.class);
        this.molType = molType;
    }

    public MoleculeType getMolType() {
        return molType;
    }


}
