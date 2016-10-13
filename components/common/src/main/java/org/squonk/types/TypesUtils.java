package org.squonk.types;

import org.squonk.dataset.Dataset;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by timbo on 28/12/15.
 */
public class TypesUtils {

    public static MoleculeObject convertBasicObjectToMoleculeObject(BasicObject bo, String structureFieldName, String format, boolean preserveUuid) {

        Map<String, Object> vals = new LinkedHashMap<>();
        String struct = null;
        for (Map.Entry<String, Object> e : bo.getValues().entrySet()) {
            if (structureFieldName.equalsIgnoreCase(e.getKey())) {
                Object o = e.getValue();
                if (o != null) {
                    struct = o.toString();
                }
            } else {
                vals.put(e.getKey(), e.getValue());
            }
        }
        return new MoleculeObject(preserveUuid ? bo.getUUID() : null, struct, format, vals);
    }

    public static Dataset<MoleculeObject> convertBasicObjectDatasetToMoleculeObjectDataset(Dataset<BasicObject> dataset, String structureFieldName, String structureFormat, boolean preserveUuid) throws IOException {
        Stream<BasicObject> stream1 = dataset.getStream();
        Stream<MoleculeObject> stream2 = stream1.map((bo) -> {
            return TypesUtils.convertBasicObjectToMoleculeObject(bo, structureFieldName, structureFormat, preserveUuid);
        });

        return new Dataset<>(MoleculeObject.class, stream2);
    }


    /** Utility function to do out best to convert to float.
     *
     * @param o
     * @return
     */
    public static java.lang.Float convertFloat(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof java.lang.Float) {
            return (java.lang.Float)o;
        } else if (o instanceof Number) {
            return ((Number)o).floatValue();
        } else {
            return new java.lang.Float(o.toString());
        }
    }

    /** Utility function to do out best to convert to double.
     *
     * @param o
     * @return
     */
    public static java.lang.Double convertDouble(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof java.lang.Double) {
            return (java.lang.Double)o;
        } else if (o instanceof Number) {
            return ((Number)o).doubleValue();
        } else {
            return new java.lang.Double(o.toString());
        }
    }

    /** Utility function to do out best to convert to integer.
     *
     * @param o
     * @return
     */
    public static java.lang.Integer convertInteger(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof java.lang.Integer) {
            return (java.lang.Integer)o;
        } else if (o instanceof Number) {
            // create via double so that we round numbers and handle scientific notation
            java.lang.Double d = ((Number)o).doubleValue();
            return d.intValue();
        } else {
            return new java.lang.Integer(o.toString());
        }
    }

}
