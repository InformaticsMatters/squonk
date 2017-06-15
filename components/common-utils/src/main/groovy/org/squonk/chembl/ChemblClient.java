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

package org.squonk.chembl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple client allowing data to be retrieved from ChEMBL REST web services.
 * Currently only supported function is to retrieve activities for a assay.
 *
 * @author timbo
 */
public class ChemblClient {

    private static final Logger LOG = Logger.getLogger(ChemblClient.class.getName());

    private static final String CHEMBL_ID = "ChemblID";

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "https://www.ebi.ac.uk";
    private static final String PATH = "/chembl/api/data/activity.json?assay_chembl_id=";
    private static final String CHEMBL_WS = "ChEMBL web services";

    private int maxIterations = 1000;

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * Retrieves activities for an assay. The results are in the form of a
     * Dataset&lt;MoleculeObject&gt;. The ChEMBL standardized values are
     * returned as a field named {prefix}_{standard_type}_{standard_units}.<br>
     * The structure is taken from the canonical_smiles field.<br>
     * A value named ChemblID containing the ChEMBL ID of the structure is taken
     * from the molecule_chembl_id field.<br>
     * A value named {prefix}_validity_comment is taken from the
     * data_validity_comment field.<br>
     * A value named {prefix}_Mod is taken from the standard_relation field if
     * the value of that field is not =.
     * <p>
     * An example of data from the service can be seen
     * <a href="https://www.ebi.ac.uk/chembl/api/data/activity.json?assay_chembl_id=CHEMBL1909156&limit=10">here</a>.
     * <br>
     * Documentation can be found <a href="https://www.ebi.ac.uk/chembl/ws">here</a>
     * <br>
     * Results are retrieved in chunks (size specified by the batchSize parameter)
     * until the entire set of activities has been obtained. To prevent infinite
     * recursion there is a limit of 1000 iterations. A batchSize of 100 is
     * generally good, but if there are over 100,000 activities you will need to
     * specify a bigger batch size.
     * <p>
     * NOTE: this method will not currently function correctly if there are
     * repeated values for the same activity type. Support for aggregate values is
     * needed for this.
     *
     * To query for assays:
     * https://www.ebi.ac.uk/chembl/api/data/assay.json?description__icontains=P450
     * To get details of an assay:
     * https://www.ebi.ac.uk/chembl/api/data/assay.json?assay_chembl_id=CHEMBL1613777
     * To get the target info:
     * https://www.ebi.ac.uk/chembl/api/data/target.json?target_chembl_id=CHEMBL3622
     * From this you can get the target components:
     * https://www.ebi.ac.uk/chembl/api/data/target_component.json?component_id=1939
     *
     *
     * @param assayId The assay ID to retrieved e.g. CHEMBL1909156
     * @param batchSize The number of activities to retrieve in each chunk.
     * @param prefix A prefix to use for the field names. e.g. if you specify H1
     * as the prefix the generated field names will be H1_IC50_nM, H1_Ki_nM etc.
     * @return A Dataset backed by a List of MoleculeObjects
     * @throws IOException
     */
    public Dataset<MoleculeObject> fetchActivitiesForAssay(String assayId, int batchSize, String prefix) throws IOException {
        if (assayId == null) {
            throw new NullPointerException("Assay ID must be defined");
        }
        LOG.info("Querying ChEMBL for " + assayId);

        DatasetMetadata<MoleculeObject> meta = new DatasetMetadata<>(MoleculeObject.class);
        String now = DatasetMetadata.formatDate();
        meta.appendDatasetHistory("Created from ChEMBL assay " + assayId);
        meta.getProperties().put(DatasetMetadata.PROP_CREATED, now);
        meta.getProperties().put(DatasetMetadata.PROP_SOURCE, BASE_URL + PATH + assayId);
        meta.getProperties().put(DatasetMetadata.PROP_DESCRIPTION, "Fetched assay " + assayId + " using ChEMBL web services");

        int iteration = 0;
        if (prefix == null) {
            prefix = assayId;
        }
        Map<String, MoleculeObject> mols = new HashMap<>();
        String path = PATH + assayId + "&limit=" + batchSize;
        JsonNode node = handleRequest(assayId, path, prefix, mols, meta);
        iteration++;
        String next = readStringValue(node, "next");
        LOG.log(Level.INFO, "Next:  {0}", next);
        while (next != null && iteration <= maxIterations) {
            node = handleRequest(assayId, next, prefix, mols, meta);
            iteration++;
            next = readStringValue(node, "next");
            LOG.log(Level.INFO, "Next:  {0}", next);
        }

        return new Dataset(MoleculeObject.class, mols.values(), meta);
    }

    private JsonNode handleRequest(String assayId, String path, String prefix, Map<String, MoleculeObject> mols, DatasetMetadata<MoleculeObject> meta) throws IOException {
        URL url = new URL(BASE_URL + path);
        LOG.info("GET: " + url.toString());

        JsonNode root = mapper.readTree(url);
        JsonNode activities = root.get("activities");

        addFieldPropertyIfNotPresent(meta, CHEMBL_ID, CHEMBL_WS, "ChEMBL property: molecule_chembl_id", String.class);

        Iterator<JsonNode> elements = activities.elements();
        while (elements.hasNext()) {
            JsonNode el = elements.next();
            String smiles = readStringValue(el, "canonical_smiles");
            String stdType = readStringValue(el, "standard_type");
            String stdRel = readStringValue(el, "standard_relation");
            String stdUnits = readStringValue(el, "standard_units");
            String stdValue = readStringValue(el, "standard_value");
            String moleculeId = readStringValue(el, "molecule_chembl_id");
            String validityComment = readStringValue(el, "data_validity_comment");

            MoleculeObject mo = mols.get(moleculeId);
            if (mo == null) {
                mo = new MoleculeObject(smiles, "smiles");
                mo.putValue(CHEMBL_ID, moleculeId);
                mols.put(moleculeId, mo);
            }

            if (validityComment != null) {
                String validityFieldName = prefix + "_validity_comment";
                addFieldPropertyIfNotPresent(meta, validityFieldName, CHEMBL_WS, assayId + " property: data_validity_comment", String.class);
                mo.putValue(validityFieldName, validityComment);
            }

            if (stdValue != null) {
                String fldName;
                if (stdUnits == null) {
                    fldName = prefix + "_" + stdType;
                } else {
                    fldName = prefix + "_" + stdType + "_" + stdUnits;
                }
                addFieldPropertyIfNotPresent(meta,  fldName, CHEMBL_WS, assayId + " properties: standard_value, standard_type, standard_units", Float.class);
                mo.putValue(fldName, new Float(stdValue));
                if (stdRel != null && !stdRel.equals("=")) {
                    String modifierFieldName = prefix + "_Mod";
                    mo.putValue(modifierFieldName, stdRel);
                    addFieldPropertyIfNotPresent(meta,  modifierFieldName, CHEMBL_WS, assayId + " property: standard_relation", String.class);
                }

//                QualifiedValue.Qualifier q = QualifiedValue.Qualifier.create(stdRel);
//                mo.putValue(fldName, new QualifiedValue(new Float(stdValue), q));
            }

        }

        JsonNode pageMeta = root.get("page_meta");
        return pageMeta;
    }

    private void addFieldPropertyIfNotPresent(DatasetMetadata<MoleculeObject> meta, String fieldName, String source, String description, Class type) {
        if (meta.getFieldMetaProp(fieldName,DatasetMetadata.PROP_CREATED) == null) {
            meta.createField(fieldName, source, description, type);
            meta.appendFieldHistory(fieldName, "Value read from " + description);
        }
    }

    private String readStringValue(JsonNode node, String name) {

        JsonNode n = node.get(name);
        if (n != null) {
            String s = n.asText();
            if (s == null || s.equals("null")) {
                return null;
            } else {
                return s;
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        ChemblClient client = new ChemblClient();
        long t0 = System.currentTimeMillis();
        // CHEMBL1613777 Cyp 2C19
        // CHEMBL1909156 H1
        Dataset<MoleculeObject> mols = client.fetchActivitiesForAssay("CHEMBL1909156", 100, "H1");
        long t1 = System.currentTimeMillis();
        int withValues = 0;
        for (MoleculeObject mo : mols.getItems()) {
            if (mo.getValues().size() > 1) {
                withValues++;
                System.out.println(mo);
            }
        }
        System.out.println("Num mols: " + mols.getItems().size());
        System.out.println(withValues + " entries found with values");
        System.out.println("Retrieval took " + (t1 - t0) + "ms");
    }

}
