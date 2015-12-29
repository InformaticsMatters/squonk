package org.squonk.chembl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.lac.types.MoleculeObject;
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

    private final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "https://www.ebi.ac.uk";

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
     * Data is retrieved in chunks (size specified by the batchSize parameter)
     * until the entire set of activities has been obtained. To prevent infinite
     * recursion there is a limit of 1000 iterations. A batchSize of 100 is
     * generally good, but if there are over 100,000 activities you will need to
     * specify a bigger batch size.
     * <p>
     * NOTE: this method will not currently function correctly if there are
     * repeated values for the same activity type. Support for aggregate values is
     * needed for this.
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

        int iteration = 0;
        if (prefix == null) {
            prefix = assayId;
        }
        Map<String, MoleculeObject> mols = new HashMap<>();
        String path = "/chembl/api/data/activity.json?assay_chembl_id=" + assayId + "&limit=" + batchSize;
        LOG.log(Level.FINE, "First: {0}", path);
        JsonNode meta = handleRequest(path, prefix, mols);
        iteration++;
        String next = readStringValue(meta, "next");
        LOG.log(Level.FINE, "Next:  {0}", next);
        while (next != null && iteration <= maxIterations) {
            meta = handleRequest(next, prefix, mols);
            iteration++;
            next = readStringValue(meta, "next");
            LOG.log(Level.FINE, "Next:  {0}", next);
        }

        return new Dataset(MoleculeObject.class, mols.values());
    }

    private JsonNode handleRequest(String path, String prefix, Map<String, MoleculeObject> mols) throws IOException {
        URL url = new URL(BASE_URL + path);

        JsonNode root = mapper.readTree(url);
        JsonNode activities = root.get("activities");

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
                mo.putValue("ChemblID", moleculeId);
                mols.put(moleculeId, mo);
            }

            if (validityComment != null) {
                mo.putValue(prefix + "_validity_comment", validityComment);
            }

            if (stdValue != null) {
                String fldName;
                if (stdUnits == null) {
                    fldName = prefix + "_" + stdType;
                } else {
                    fldName = prefix + "_" + stdType + "_" + stdUnits;
                }

                mo.putValue(fldName, new Float(stdValue));
                if (stdRel != null && !stdRel.equals("=")) {
                    mo.putValue(prefix + "_Mod", stdRel);
                    System.out.println("Mod: " + stdRel + " Value: " + stdValue);
                }

//                QualifiedValue.Qualifier q = QualifiedValue.Qualifier.create(stdRel);
//                mo.putValue(fldName, new QualifiedValue(new Float(stdValue), q));
            }

        }

        JsonNode pageMeta = root.get("page_meta");
        return pageMeta;
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
