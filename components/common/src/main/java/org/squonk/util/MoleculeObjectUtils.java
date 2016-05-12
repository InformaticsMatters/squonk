package org.squonk.util;

import com.im.lac.types.MoleculeObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by timbo on 09/05/2016.
 */
public class MoleculeObjectUtils {

    public static Stream<MoleculeObject> deduplicate(
            Stream<MoleculeObject> mols,
            String canonicalSmilesField,
            List<String> keepFirstFields,
            List<String> keepLastFields,
            List<String> appendFields) {

        if (canonicalSmilesField == null) {
            throw new NullPointerException("Must specify canonicalValueName");
        }


        Map<Object, MoleculeObject> results = new LinkedHashMap<>();

        mols.forEachOrdered((mo) -> {
            String smiles = mo.getValue(canonicalSmilesField, String.class);
            if (smiles != null) {
                MoleculeObject existing = results.get(smiles);
                if (existing != null) {
                    for (Map.Entry<String,Object> e :  mo.getValues().entrySet()) {
                        String k = e.getKey();
                        Object v = e.getValue();
                        if (k.equals(canonicalSmilesField)) {
                            continue;
                        } else if (appendFields.contains(k)) {
                            List l = (List)existing.getValue(k);
                            if (l == null) {
                                l = new ArrayList();
                                existing.putValue(k, l);
                            }
                            l.add(v);
                        }
                        if (keepLastFields.contains(k)) {
                            existing.putValue(k, v);
                        }
                        if (keepFirstFields.contains(k)) {
                            existing.getValues().putIfAbsent(k, v);
                        }
                    }
                } else {
                    MoleculeObject first = new MoleculeObject(smiles, "smiles");
                    for (Map.Entry<String,Object> e :  mo.getValues().entrySet()) {
                        String k = e.getKey();
                        if (k.equals(canonicalSmilesField)) {
                            continue;
                        } else if (appendFields.contains(k)) {
                            List l = new ArrayList();
                            l.add(e.getValue());
                            first.putValue(k, l);
                        } else if (keepFirstFields.contains(k) || keepLastFields.contains(k)) {
                            first.putValue(k, e.getValue());
                        }
                    }
                    results.put(smiles, first);
                }
            }
        });

        return results.values().stream();
    }

}
