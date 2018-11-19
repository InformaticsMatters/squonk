/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.types;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.squonk.util.IOUtils;
import java.util.UUID;

/**
 * Represents a molecule in a platform neutral way allowing instances to be passed between different
 * chemistry implementations and properties created or used.
 *
 * TODO - does the Charset of the source molecule need to be handled?
 *
 * @author timbo
 */
@JsonIgnoreProperties({"representations", "representation", "value"})
@JsonInclude(Include.NON_EMPTY)
public class MoleculeObject extends BasicObject {

    private static final long serialVersionUID = 1L;

    public static final String INVALID_FORMAT_PROP = "ERROR_INVALID_FORMAT";

    public static final String FORMAT_SMILES = "smiles";
    public static final String FORMAT_MOLFILE = "mol";
    public static final String FORMAT_INCHI = "inchi";

    /**
     * The source of the molecule in its original form. This will be String containing smiles, InCHI
     * or Molfile.
     */
    private String source;

    /**
     * The input format e.g. smiles, sdf. Some constants are defined to assist with this. Note that
     * this field may not always be set.
     */
    private String format;

    /**
     * A map that allows different representations of a molecule to be stored for future use,
     * avoiding the need for unnecessary instantiation of molecules. These representations are
     * vendor dependent and not interoperable. For instance if using ChemAxon code then you can
     * store a chemaxon.struc.Molecule instance generated from the source as a representation that
     * can be re-used. In addition different representations can be stored e.g. ones with implicit
     * hydrogens added or the molecule converted to aromatic form. It is up to the caller to choose
     * the key to use to store the representation but it is recommended to follow a convention of
     * using the fully qualified class name for an exact representation of the source. e.g. put the
     * ChemAxon Molecule representation of the source under the key of "chemaxon.struc.Molecule".
     * Note that this property is transient and will not persist across remote calls so the presence
     * of any particular representation cannot always be guaranteed.
     */
    private final transient Map<Object, Object> representations = new HashMap<>();

    /**
     * Base constructor that creates a MoleculeObject with the specified UUID
     *
     * @param uuid Unique and persistent identifier for this MoleculeObject
     */
    public MoleculeObject(UUID uuid) {
        super(uuid);
    }

    /**
     * Default constructor for now instance, with random UUID generated.
     *
     */
    public MoleculeObject() {
        super();
    }

    /**
     * Constructor for now instance with structure source
     *
     * @param source The molecule in some recognised but unspecified format
     */
    public MoleculeObject(String source) {
        super();
        this.source = source;
    }

    /**
     * Constructor for now instance with structure source and format
     *
     * @param source The molecule in some recognised and specified format
     * @param format smiles, mol etc.
     */
    public MoleculeObject(String source, String format) {
        super();
        this.source = source;
        this.format = format;
    }

    /**
     * Constructor for now instance with additional properties
     *
     * @param source The molecule in some recognised format
     * @param format smiles, mol etc.
     * @param values Properties for the molecule
     */
    public MoleculeObject(String source, String format, Map<String, Object> values) {
        super(values);
        this.source = source;
        this.format = format;
    }


    /**
     * Constructor for re-generating a MoleculeObject with an existing UUID
     *
     * @param uuid
     * @param source
     * @param format
     */
    public MoleculeObject(UUID uuid, String source, String format) {
        super(uuid);
        this.source = source;
        this.format = format;
    }

    /**
     * Constructor for re-generating a MoleculeObject with an existing UUID
     *
     * @param uuid
     * @param source
     * @param format
     * @param values
     */
    public MoleculeObject(UUID uuid, String source, String format, Map<String, Object> values) {
        super(uuid, values);
        this.source = source;
        this.format = format;
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

    @Override
    public MoleculeObject clone() {
        return new MoleculeObject(uuid, source, format, values);
    }

    @Override
    public void merge(BasicObject other, boolean ignoreValues) {
        if (other instanceof MoleculeObject) {
            MoleculeObject mo = (MoleculeObject)other;
            this.source = mo.getSource();
            this.format = mo.getFormat();
            super.merge(other, ignoreValues);
        } else {
             throw new IllegalStateException("Incompatible types. Required MoleculeObject found " + other.getClass().getSimpleName());
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("MoleculeObject uuid: ")
                .append(uuid)
                .append(" source: ")
                .append(IOUtils.truncateString(source, 100))
                .append(" format: ")
                .append(format)
                .append(" values: [");
        for (Map.Entry<String, Object> e : values.entrySet()) {
            b.append(e.getKey())
                    .append(":")
                    .append(e.getValue())
                    .append(" ");
        }
        b.append("]");

        return b.toString();
    }

    public static void main(String[] args) {
        Map<String, Object> props = new HashMap<>();
        props.put("hello", "world");
        MoleculeObject mo = new MoleculeObject("C", "smiles", props);
    }

}
