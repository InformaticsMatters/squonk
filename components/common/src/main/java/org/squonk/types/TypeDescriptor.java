package org.squonk.types;

import org.squonk.dataset.Dataset;

import java.util.HashMap;
import java.util.Map;
import static org.squonk.util.CommonMimeTypes.*;

public class TypeDescriptor {

    private final static Map<String, TypeDescriptor> typeDescriptors = new HashMap<>();

    static {
        registerMimeType(MIME_TYPE_DATASET_BASIC_JSON, Dataset.class, BasicObject.class);
        registerMimeType(MIME_TYPE_DATASET_MOLECULE_JSON, Dataset.class, MoleculeObject.class);
        registerMimeType(MIME_TYPE_BASIC_OBJECT_JSON, BasicObject.class);
        registerMimeType(MIME_TYPE_MOLECULE_OBJECT_JSON, MoleculeObject.class);
        registerMimeType(MIME_TYPE_MDL_MOLFILE, MolFile.class);
        registerMimeType(MIME_TYPE_MDL_SDF, SDFile.class);
        registerMimeType(MIME_TYPE_PDB, PDBFile.class);
        registerMimeType(MIME_TYPE_TRIPOS_MOL2, Mol2File.class);
        registerMimeType(MIME_TYPE_CPSIGN_TRAIN_RESULT, CPSignTrainResult.class);
        registerMimeType(MIME_TYPE_ZIP_FILE, ZipFile.class);
        registerMimeType(MIME_TYPE_PNG, PngImageFile.class);
    }


    private static void registerMimeType(String mimeType, Class primaryType) {
        registerMimeType(mimeType, primaryType, null);

    }

    private static void registerMimeType(String mimeType, Class primaryType, Class genericType) {
        typeDescriptors.put(mimeType, new TypeDescriptor(primaryType, genericType));
    }

    public static Class resolvePrimaryType(String mediaType) {
        TypeDescriptor t = typeDescriptors.get(mediaType);
        return t == null ? null : t.getPrimaryType();
    }

    public static Class resolveGenericType(String mediaType) {
        TypeDescriptor t = typeDescriptors.get(mediaType);
        return t == null ? null : t.getSecondaryType();
    }

    public static String resolveMediaType(Class type) {
        return resolveMediaType(type, null);
    }

    public static String resolveMediaType(Class primaryType, Class secondaryType) {
        for (Map.Entry<String,TypeDescriptor> e: typeDescriptors.entrySet()) {
            TypeDescriptor t = e.getValue();
            if (t.getPrimaryType() == primaryType && t.getSecondaryType() == secondaryType) {
                return e.getKey();
            }
        }
        return null;
    }


    Class primaryType;
    Class secondaryType;

    TypeDescriptor(Class primaryType, Class secondaryType) {
        assert primaryType != null;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
    }

    public Class getPrimaryType() {
        return primaryType;
    }

    public Class getSecondaryType() {
        return secondaryType;
    }

    @Override
    public int hashCode() {
        if (secondaryType == null) {
            return primaryType.getName().hashCode();
        } else {
            return (primaryType.getName() + "+" + secondaryType.getName()).hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof TypeDescriptor)) {
            return false;
        }
        TypeDescriptor t = (TypeDescriptor)o;

        if (primaryType != t.getPrimaryType()) {
            return false;
        }
        if (secondaryType == null && t.getSecondaryType() == null) {
            return true;
        }
        if (secondaryType == t.getSecondaryType()) {
            return true;
        }
        return false;
    }
}
