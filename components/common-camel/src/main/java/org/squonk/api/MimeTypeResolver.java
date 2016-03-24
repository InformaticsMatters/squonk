package org.squonk.api;

/**
 * Created by timbo on 20/03/2016.
 */
public interface MimeTypeResolver {

    // For chemical mime types look here https://www.ch.ic.ac.uk/chemime/

    public static final String MIME_TYPE_DATASET_BASIC_JSON = "application/x-squonk-dataset-basic-json";
    public static final String MIME_TYPE_DATASET_MOLECULE_JSON = "application/x-squonk-dataset-molecule-json";
    public static final String MIME_TYPE_BASIC_OBJECT_JSON = "application/x-squonk-basic-object-json";
    public static final String MIME_TYPE_MOLECULE_OBJECT_JSON = "application/x-squonk-molecule-object-json";
    public static final String MIME_TYPE_MDL_MOLFILE = "chemical/x-mdl-molfile";
    public static final String MIME_TYPE_MDL_SDF = "chemical/x-mdl-sdfile";
    public static final String MIME_TYPE_DAYLIGHT_SMILES = "chemical/x-daylight-smiles";
    public static final String MIME_TYPE_TRIPOS_MOL2 = "chemical/x-mol2";
    public static final String MIME_TYPE_PDB = "chemical/x-pdb";


    Class resolvePrimaryType(String mimeType);

    Class resolveGenericType(String mimeType);

    default HttpHandler createHttpHandler(String mimeType) {
        return createHttpHandler(resolvePrimaryType(mimeType), resolveGenericType(mimeType));
    }

    default HttpHandler createHttpHandler(Class primaryType) {
        return createHttpHandler(primaryType, null);
    }

    HttpHandler createHttpHandler(Class primaryType, Class genericType);

    default VariableHandler createVariableHandler(String mimeType) {
        return createVariableHandler(resolvePrimaryType(mimeType), resolveGenericType(mimeType));
    }

    default VariableHandler createVariableHandler(Class primaryType) {
        return createVariableHandler(primaryType, null);
    }

    VariableHandler createVariableHandler(Class cls, Class genericType);
}



