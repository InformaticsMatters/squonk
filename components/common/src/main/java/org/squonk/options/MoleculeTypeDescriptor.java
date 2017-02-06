package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.types.Structure;

import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 03/02/16.
 */
public class MoleculeTypeDescriptor extends SimpleTypeDescriptor<Structure> {

    private static final Logger LOG = Logger.getLogger(MoleculeTypeDescriptor.class.getName());

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
            LOG.info("Body option. Putting as Structure");
        } else if (key.startsWith("query.") || key.startsWith("header.") || key.startsWith("arg.")) {
            options.put(key + "_source", value.getSource());
            options.put(key + "_format", value.getFormat());
            LOG.info("Param option. Putting as individual props");
        } else {
            // this may lead to problems ...
            LOG.warning("WARNING: unable to put option correctly so just using source as String");
            options.put(key, value.getSource());
        }
    }

    public Structure readOptionValue(Map<String, Object> options, String key) {
        Object source = options.get(key + "_source");
        Object format = options.get(key + "_format");
        if (source != null && format != null) {
            return new Structure(source.toString(), format.toString());
        }
        LOG.warning("WARNING: failed to read Structure option for " + key + ". Keys present were: "
                + options.keySet().stream().collect(Collectors.joining(",")));
        return null;
    }


}
