package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.types.Structure;

import java.util.Map;

/**
 * Created by timbo on 03/02/16.
 */
public class MoleculeTypeDescriptor extends SimpleTypeDescriptor<Structure> {

    public enum MoleculeType {DISCRETE, QUERY, REACTION}

    public static final MoleculeTypeDescriptor QUERY = new MoleculeTypeDescriptor(
            MoleculeTypeDescriptor.MoleculeType.QUERY,
            new String[]{"mol","smarts","smiles"});

    public static final MoleculeTypeDescriptor DISCRETE = new MoleculeTypeDescriptor(
            MoleculeTypeDescriptor.MoleculeType.DISCRETE,
            new String[]{"smiles"});

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

    public void putOptionValue(Map<String, Object> options, String key, Structure value) {
        if ("body".equals(key)) {
            options.put(key, value);
        } else if (key.startsWith("query.") || key.startsWith("header.")) {
            options.put(key + "_source", value.getSource());
            options.put(key + "_format", value.getFormat());
        }
    }

    public Structure readOptionValue(Map<String, Object> options, String key) {
        Object source = options.get(key + "_source");
        Object format = options.get(key + "_format");
        if (source != null && format != null) {
            return new Structure(source.toString(), format.toString());
        }
        return null;
    }


}
