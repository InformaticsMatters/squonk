package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.types.Structure;

/**
 * Created by timbo on 03/02/16.
 */
public class MoleculeTypeDescriptor <T> extends SimpleTypeDescriptor<Structure> {

    public enum MoleculeType {DISCRETE, QUERY, REACTION}

    private final MoleculeType molType;
    private final String[] formats;

    public MoleculeTypeDescriptor(@JsonProperty("molType") MoleculeType molType, @JsonProperty("formats") String[] formats) {
        super(Structure.class);
        this.molType = molType;
        this.formats = formats;
    }

    public MoleculeType getMolType() {
        return molType;
    }

    public String[] getFormats() {
        return formats;
    }


}
