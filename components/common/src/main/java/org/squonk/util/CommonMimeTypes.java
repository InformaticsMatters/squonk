package org.squonk.util;

/**
 * Created by timbo on 13/06/16.
 */
public interface CommonMimeTypes {

    // For chemical mime types look here https://www.ch.ic.ac.uk/chemime/

    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String MIME_TYPE_TEXT_CSV = "text/csv";
    public static final String MIME_TYPE_DATASET_BASIC_JSON = "application/x-squonk-dataset-basic+json";
    public static final String MIME_TYPE_DATASET_MOLECULE_JSON = "application/x-squonk-dataset-molecule+json";
    public static final String MIME_TYPE_BASIC_OBJECT_JSON = "application/x-squonk-basic-object+json";
    public static final String MIME_TYPE_MOLECULE_OBJECT_JSON = "application/x-squonk-molecule-object+json";
    public static final String MIME_TYPE_MDL_MOLFILE = "chemical/x-mdl-molfile";
    public static final String MIME_TYPE_MDL_SDF = "chemical/x-mdl-sdfile";
    public static final String MIME_TYPE_DAYLIGHT_SMILES = "chemical/x-daylight-smiles";
    public static final String MIME_TYPE_DAYLIGHT_SMARTS = "chemical/x-daylight-smarts"; // this is not a recognised mime type
    public static final String MIME_TYPE_TRIPOS_MOL2 = "chemical/x-mol2";
    public static final String MIME_TYPE_PDB = "chemical/x-pdb";
    public static final String MIME_TYPE_SVG = "image/svg+xml";
    public static final String MIME_TYPE_PNG = "image/png";
    public static final String MIME_TYPE_CPSIGN_TRAIN_RESULT = "application/x-squonk-cpsign-train+json";;

}
