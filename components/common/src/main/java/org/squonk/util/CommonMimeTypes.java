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

package org.squonk.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by timbo on 13/06/16.
 */
public interface CommonMimeTypes {

    // For chemical mime types look here https://www.ch.ic.ac.uk/chemime/

    String MIME_TYPE_TEXT_PLAIN = "text/plain";
    String MIME_TYPE_TEXT_CSV = "text/csv";
    String MIME_TYPE_JSON = "application/json";
    String MIME_TYPE_MULTIPART_MIXED = "multipart/mixed";
    String MIME_TYPE_DATASET_METADATA = "application/x-squonk-dataset-metadata+json";
    String MIME_TYPE_DATASET_BASIC_JSON = "application/x-squonk-dataset-basic+json";
    String MIME_TYPE_DATASET_MOLECULE_JSON = "application/x-squonk-dataset-molecule+json";
    String MIME_TYPE_BASIC_OBJECT_JSON = "application/x-squonk-basic-object+json";
    String MIME_TYPE_MOLECULE_OBJECT_JSON = "application/x-squonk-molecule-object+json";

    // when using thes make sure you add +json or +yaml to the end
    String SERVICE_DESCRIPTOR_BASE = "application/x-squonk-service-descriptor-";
    String MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER = SERVICE_DESCRIPTOR_BASE + "docker";
    String MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER_JSON = MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER + "+json";
    String MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER_YAML = MIME_TYPE_SERVICE_DESCRIPTOR_DOCKER + "+yaml";
    String MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW = SERVICE_DESCRIPTOR_BASE + "nextflow";
    String MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW_JSON = MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW + "+json";
    String MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW_YAML = MIME_TYPE_SERVICE_DESCRIPTOR_NEXTFLOW + "+yaml";
    String MIME_TYPE_SERVICE_DESCRIPTOR_SET = SERVICE_DESCRIPTOR_BASE + "set";
    String MIME_TYPE_SERVICE_DESCRIPTOR_SET_JSON = MIME_TYPE_SERVICE_DESCRIPTOR_SET + "+json";
    String MIME_TYPE_SERVICE_DESCRIPTOR_SET_YAML = MIME_TYPE_SERVICE_DESCRIPTOR_SET + "+yaml";

    String MIME_TYPE_MDL_MOLFILE = "chemical/x-mdl-molfile";
    String MIME_TYPE_MDL_SDF = "chemical/x-mdl-sdfile";
    String MIME_TYPE_DAYLIGHT_SMILES = "chemical/x-daylight-smiles";
    String MIME_TYPE_DAYLIGHT_SMARTS = "chemical/x-daylight-smarts"; // this is not a generally recognised mime type
    String MIME_TYPE_TRIPOS_MOL2 = "chemical/x-mol2";
    String MIME_TYPE_PDB = "chemical/x-pdb";
    String MIME_TYPE_SVG = "image/svg+xml";
    String MIME_TYPE_PNG = "image/png";
    String MIME_TYPE_CPSIGN_TRAIN_RESULT = "application/x-squonk-cpsign-train+json";
    String MIME_TYPE_ZIP_FILE = "application/zip";


    Map<String, String[]> EXTENSIONS = Collections.unmodifiableMap(new HashMap() {{
        put(MIME_TYPE_TEXT_PLAIN, new String[] {"txt"});
        put(MIME_TYPE_DAYLIGHT_SMILES, new String[] {"smiles", "smi"});
        put(MIME_TYPE_PDB, new String[] {"pdb"});
        put(MIME_TYPE_MDL_SDF, new String[] {"sdf"});
        put(MIME_TYPE_MDL_MOLFILE, new String[] {"mol"});
        put(MIME_TYPE_TRIPOS_MOL2, new String[] {"mol2"});
        put(MIME_TYPE_SVG, new String[] {"svg"});
        put(MIME_TYPE_PNG, new String[] {"png"});
        put(MIME_TYPE_ZIP_FILE, new String[] {"zip"});
    }});

}
