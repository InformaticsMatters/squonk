package com.im.lac.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a molecule in a platform neutral way allowing instances to be
 * passed between different chemistry implementations and properties created or
 * used.
 *
 * TODO - does the Charset of the source molecule need to be handled?
 *
 * @author timbo
 */
public class MoleculeObject implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FORMAT_SMILES = "smiles";
    public static final String FORMAT_MOLFILE = "mol";
    public static final String FORMAT_INCHI = "inchi";

    /**
     * The source of the molecule in its original form. Usually this will be
     * String containing smiles, InCHI or Molfile, but some formats like CDX are
     * binary formats so we handle as a byte array to allow those formats to be
     * supported.
     */
    private byte[] source;

    /**
     * The input format e.g. smiles, sdf. Some constants are defined to assist
     * with this.
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
     * Note that the keys and values must be Serializable so that these
     * properties can be used in remote implementations. The properties are set
     * and read using the getValue(), putValue() and related methods.
     */
    private Map<Serializable, Serializable> properties;

    /**
     * For serialization only.
     */
    public MoleculeObject() {
        representations = new HashMap<>();
        properties = new HashMap<>();
    }

    public MoleculeObject(String source) {
        this(source.getBytes());
    }

    public MoleculeObject(String source, String format) {
        this(source.getBytes(), format);
    }

    public MoleculeObject(byte[] source) {
        this();
        this.source = source;
    }

    public MoleculeObject(byte[] source, String format) {
        this();
        this.source = source;
        this.format = format;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        representations = new HashMap<>();
    }

    public byte[] getSourceAsBytes() {
        return source;
    }

    public String getSourceAsString() {
        return new String(source);
    }

    public String getFormat() {
        return format;
    }

    /**
     * Get the format if defined, or else the supplied default
     *
     * @param def
     * @return
     */
    public String getFormat(String def) {
        if (format != null) {
            return format;
        } else {
            return def;
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

    public Object getValue(Serializable key) {
        return properties.get(key);
    }

    public Map<Serializable, Serializable> getValues() {
        return properties;
    }

    public <T> T getValue(Serializable key, Class<T> type) {
        return (T) properties.get(key);
    }

    public boolean hasValue(Serializable key) {
        return properties.containsKey(key);
    }

    /**
     * Put this property. The key and the value should be Serializable, but we
     * allow any Objects here. If they are not Serializable then the .toString()
     * representation is used.
     *
     * @param key
     * @param value
     * @return
     */
    public Serializable putValue(Object key, Object value) {
        Serializable k, v;
        if (key instanceof Serializable) {
            k = (Serializable) key;
        } else {
            k = key.toString();
        }
        if (value instanceof Serializable) {
            v = (Serializable) value;
        } else {
            v = value.toString();
        }
        Serializable o = properties.put(k, v);
        return o;
    }

    public void putValues(Map<? extends Object, ? extends Object> values) {
        for (Map.Entry<? extends Object, ? extends Object> e : values.entrySet()) {
            putValue(e.getKey(), e.getValue());
        }
    }

}
