package com.im.lac.types;

import com.squonk.dataset.Dataset;

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
}
