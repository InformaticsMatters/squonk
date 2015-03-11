package com.im.lac.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a molecule in a platform neutral way allowing instances to be
 * passed between different chemistry implementations and properties created or
 * used.
 *
 * TODO - does the Charset of the source molecule need to be handled?
 *
 * @author timbo
 */
@JsonIgnoreProperties({"representations", "representation", "value"})
@JsonInclude(Include.NON_EMPTY)
public class MoleculeObject implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FORMAT_SMILES = "smiles";
    public static final String FORMAT_MOLFILE = "mol";
    public static final String FORMAT_INCHI = "inchi";

    /**
     * The source of the molecule in its original form. Usually this will be
     * String containing smiles, InCHI or Molfile.
     */
    private String source;

    /**
     * The input format e.g. smiles, sdf. Some constants are defined to assist
     * with this. Note that this field may not always be set.
     */
    private String format;

    /**
     * A map that allows different representations of a molecule to be stored
     * for future use, avoiding the need for unnecessary instantiation of
     * molecules. These representations are vendor dependent and not
     * interoperable. For instance if using ChemAxon code then you can store a
     * chemaxon.struc.Molecule instance generated from the source as a
     * representation that can be re-used. In addition different representations
     * can be stored e.g. ones with implicit hydrogens added or the molecule
     * converted to aromatic form. It is up to the caller to choose the key to
     * use to store the representation but it is recommended to follow a
     * convention of using the fully qualified class name for an exact
     * representation of the source. e.g. put the ChemAxon Molecule
     * representation of the source under the key of "chemaxon.struc.Molecule".
     * Note that this property is transient and will not persist across remote
     * calls so the presence of any particular representation cannot always be
     * guaranteed.
     */
    private transient Map<Object, Object> representations;

    /**
     * Properties of the molecule. These are shared across all chemistry
     * implementations allowing different implementations to be interoperable.
     * Note that the keys must be Strings and values must be writable as simple JSON 
     * types so that these properties can be used in remote implementations. The 
     * properties are set and read using the getValue(), putValue() and related methods.
     */
    private Map<String, Object> values;

    /**
     * For serialization only.
     */
    public MoleculeObject() {
        representations = new HashMap<>();
        values = new HashMap<>();
    }

    public MoleculeObject(String source) {
        this(source, null);
    }

    public MoleculeObject(String source, String format) {
        this();
        this.source = source;
        this.format = format;
    }
    
    public MoleculeObject(String source, String format, Map<String,Object> props) {
        this();
        this.source = source;
        this.format = format;
        this.values.putAll(props);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        representations = new HashMap<>();
    }

    public String getSource() {
        return source;
    }

    public String getFormat() {
        return format;
    }

    /**
     * Get the format if defined, or else the supplied default
     *
     * @param defaultValue
     * @return
     */
    public String getFormat(String defaultValue) {
        if (format != null) {
            return format;
        } else {
            return defaultValue;
        }
    }

    public Object getRepresentation(Object key) {
        return representations.get(key);
    }

    public Map<Object, Object> getRepresentations() {
        return representations;
    }

    public <T> T getRepresentation(Object key, Class<T> type) {
        return (T) representations.get(key);
    }

    public boolean hasRepresentation(Object key) {
        return representations.containsKey(key);
    }

    public Object putRepresentation(Object key, Object value) {
        return representations.put(key, value);
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public <T> T getValue(String key, Class<T> type) {
        return (T) values.get(key);
    }

    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    public Object putValue(String key, Object value) {
        return values.put(key, value);
    }

    public void putValues(Map<String, Object> values) {
        this.values.putAll(values);
    }

}
