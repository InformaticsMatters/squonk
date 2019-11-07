/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

package org.squonk.execution.steps.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;
import org.squonk.core.DefaultServiceDescriptor;
import org.squonk.core.ServiceConfig;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.execution.steps.StepDefinitionConstants;
import org.squonk.io.IODescriptors;
import org.squonk.options.OptionDescriptor;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Finds related molecules in the fragment network.
 *
 * @author timbo
 */
public class FragnetExpansionStep extends AbstractDatasetStep<MoleculeObject, MoleculeObject> {

    private static final String BASE_URL = IOUtils.getConfiguration(
            "FRAGNET_SEARCH_BASE_URL",
            "http://localhost:8080/fragnet-search");

    private static final Logger LOG = Logger.getLogger(FragnetExpansionStep.class.getName());

    public static final String OPTION_HOPS = StepDefinitionConstants.FragnetExpansion.OPTION_HOPS;
    public static final String OPTION_HAC = StepDefinitionConstants.FragnetExpansion.OPTION_HAC;
    public static final String OPTION_RAC = StepDefinitionConstants.FragnetExpansion.OPTION_RAC;

    public static DefaultServiceDescriptor SERVICE_DESCRIPTOR = new DefaultServiceDescriptor(
            "core.dataset.fragnet.expansion.v1",
            "Fragnet expansion",
            "Expand using fragment network",
            new String[]{"fragnet", "expand", "dataset", "fragmentnetwork", "similarity"},
            null, "icons/molecule_generator.png",
            ServiceConfig.Status.ACTIVE,
            new Date(),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_INPUT_DATASET),
            IODescriptors.createBasicObjectDatasetArray(StepDefinitionConstants.VARIABLE_OUTPUT_DATASET),
            new OptionDescriptor[]{
                    new OptionDescriptor<>(String.class, OPTION_HOPS, "Number of hops", "The number of fragment network hops", OptionDescriptor.Mode.User)
                            .withValues(new String[]{"1", "2"})
                            .withDefaultValue("1")
                            .withMinMaxValues(1, 1),
                    new OptionDescriptor<>(Integer.class, OPTION_HAC, "HAC change", "Change in heavy atom count", OptionDescriptor.Mode.User)
                            .withDefaultValue(3)
                            .withMinMaxValues(1, 1),
                    new OptionDescriptor<>(Integer.class, OPTION_RAC, "RAC change", "Change in ring atom count", OptionDescriptor.Mode.User)
                            .withDefaultValue(1)
                            .withMinMaxValues(1, 1)
            },
            null, null, null,
            FragnetExpansionStep.class.getName()
    );

    /**
     * Performs the expansion of the input dataset
     *
     * @param input The dataset with query molecules
     * @throws Exception
     */
    @Override
    protected Dataset<MoleculeObject> doExecuteWithDataset(Dataset<MoleculeObject> input, CamelContext context) throws Exception {

        if (!isServiceRunning()) {
            throw new IllegalStateException("Fragnet Search service is not responding");
        }

        TypeConverter converter = findTypeConverter(context);
        Integer hops = new Integer(getOption(OPTION_HOPS, Integer.class, converter, 0));
        Integer hac = getOption(OPTION_HAC, Integer.class, converter);
        Integer rac = getOption(OPTION_RAC, Integer.class, converter);

        statusMessage = "Reading inputs ...";

        Map<String,MoleculeObject> all = new LinkedHashMap<>();
        int count = 0;
        for (MoleculeObject mo : input.getItems()) {
            LOG.info("Processing mol " + count);
            statusMessage = "Processing mol " + count;
            List<MoleculeObject> mols = expandMolecule(mo, hops, hac, rac);
            for (MoleculeObject result : mols) {
                MoleculeObject existing = all.get(result.getSource());
                if (existing == null) {
                    Set<String> ids = new LinkedHashSet<>();
                    ids.add(result.getValue("src_mol", String.class));
                    result.getValues().remove("src_mol");
                    result.putValue("src_mols", ids);
                    all.put(result.getSource(), result);
                } else {
                    Set ids = (Set) existing.getValue("src_mols");
                    ids.add(result.getValue("src_mol"));
                }
            }
            count++;
        }

        statusMessage = "Processing complete";

        // convert to string
        for (MoleculeObject mo : all.values()) {
            Set<String> ids = (Set<String>)mo.getValue("src_mols");
            String s = ids.stream().sequential().collect(Collectors.joining("\n"));
            mo.putValue("src_mols", s);
        }

        statusMessage = "Generating metadata";

        Map<String, Class> valueClassMappings = new LinkedHashMap<>();
        valueClassMappings.put("src_mols", String.class);
        valueClassMappings.put("hac", Integer.class);
        valueClassMappings.put("rac", Integer.class);
        valueClassMappings.put("neighbours", Integer.class);
        valueClassMappings.put("cmpd_ids", String.class);

        DatasetMetadata<MoleculeObject> outputMeta = new DatasetMetadata<>(MoleculeObject.class, valueClassMappings, all.size());
        outputMeta.appendDatasetHistory("Fragment Network expansion from dataset");
        outputMeta.appendDatasetProperty("fragnet_endpoint", BASE_URL);
        outputMeta.appendDatasetProperty("param_hops", hops);
        outputMeta.appendDatasetProperty("param_hac", hac);
        outputMeta.appendDatasetProperty("param_rac", rac);

        statusMessage = "Expansion complete. " + all.size() + " results";

        return new MoleculeObjectDataset(all.values(), outputMeta).getDataset();
    }

    protected boolean isServiceRunning() {

        String u = BASE_URL + "/rest/v2/ping";
        try {
            URL url = new URL(u);
            LOG.fine("URL: " + url.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            return status == 200;
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to connect to FragnetSearch at " + u, e);
            return false;
        }
    }

    /**
     * Do the expansion of a simple molecule.
     *
     * @param mol
     * @param hops
     * @param hac
     * @param rac
     * @return
     * @throws IOException
     */
    protected List<MoleculeObject> expandMolecule(MoleculeObject mol, int hops, int hac, int rac) throws IOException {

        LOG.fine("Expanding molecule " + mol);

        String format = mol.getFormat();

        if (MoleculeObject.FORMAT_SMILES.equals(format)) {
            return doGet(mol, hops, hac, rac);
        } else if (MoleculeObject.FORMAT_MOLFILE.equals(format)) {
            return doPost(mol, CommonMimeTypes.MIME_TYPE_MDL_MOLFILE, hops, hac, rac);
        } else {
            throw new IllegalArgumentException("Can't handle format " + mol.getFormat());
        }
    }

    /**
     * Do the HTTP GET request. The input must be SMILES.
     *
     * @param srcMol
     * @param hops
     * @param hac
     * @param rac
     * @return
     * @throws IOException
     */
    private List<MoleculeObject> doGet(MoleculeObject srcMol, int hops, int hac, int rac) throws IOException {

        if (!srcMol.getFormat().equals("smiles")) {
            throw new IllegalArgumentException("Not in SMILES format");
        }

        String query = URLEncoder.encode(srcMol.getSource(), StandardCharsets.UTF_8.toString()) + buildQueryPart(hops, hac, rac);
        URL url = new URL(BASE_URL + "/rest/v2/search/expand/" + query);

        LOG.fine("GET: " + url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        int status = con.getResponseCode();
        LOG.fine("Response code: " + status);
        if (status >= 200 && status < 300) {
            List<MoleculeObject> mols = readResponse(con.getInputStream(), srcMol);
            return mols;
        } if (status == 404) {
            // mol not found
            LOG.info("No molecules found");
            return Collections.emptyList();
        } else {
            String msg = IOUtils.convertStreamToString(con.getInputStream());
            throw new IOException("Bad response: " + statusMessage + " " + msg);
        }
    }

    /**
     * Do the HTTP POST request. The input must be in Molfile format.
     *
     * @param srcMol
     * @param mimeType The mime type of the input. Currently only chemical/x-mdl-molfile is supported by the expansion service.
     * @param hops
     * @param hac
     * @param rac
     * @return
     * @throws IOException
     */
    private List<MoleculeObject> doPost(MoleculeObject srcMol, String mimeType, int hops, int hac, int rac) throws IOException {
        String query = buildQueryPart(hops, hac, rac);
        URL url = new URL(BASE_URL + query);

        LOG.fine("POST: " + url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", mimeType);
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = srcMol.getSource().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = con.getResponseCode();
        LOG.fine("Response code: " + status);
        if (status >= 200 && status < 300) {
            List<MoleculeObject> mols = readResponse(con.getInputStream(), srcMol);
            return mols;
        } if (status == 404) {
            // mol not found
            LOG.info("No molecules found");
            return Collections.emptyList();
        } else {
            String msg = IOUtils.convertStreamToString(con.getInputStream());
            throw new IOException("Bad response: " + statusMessage + " " + msg);
        }

    }

    private String buildQueryPart(int hops, int hac, int rac) {
        return "?hops=" + hops + "&hac=" + hac + "&rac=" + rac;
    }

    /**
     * Read the HTTP response. Members JSON will look like this:
     * {
     * "smiles": "Brc1ccc(Nc2nc3ccccc3o2)cc1",
     * "props": {
     * "chac": 15,
     * "neighbours": 9,
     * "hac": 17
     * },
     * "cmpd_ids": [
     * "MOLPORT:009-215-945"
     * ]
     * }
     */
    private List<MoleculeObject> readResponse(InputStream is, MoleculeObject srcMol) throws IOException {
        List<MoleculeObject> mols = new ArrayList<>();

        JsonNode json = JsonHandler.getInstance().getObjectMapper().readTree(is);
        LOG.fine("JSON elements: " + json.size());

        Iterator<JsonNode> it1 = json.get("members").elements();
        while (it1.hasNext()) {
            JsonNode member = it1.next();
            String s = member.get("smiles").asText();
            LOG.fine("SMILES: " + s);
            MoleculeObject mo = new MoleculeObject(s, "smiles");
            mo.putValue("src_mol", srcMol.getUUID().toString());
            JsonNode props = member.get("props");
            Iterator<String> it2 = props.fieldNames();
            while (it2.hasNext()) {
                String name = it2.next();
                if ("hac".equals(name) || "rac".equals(name) || "neighbours".equals(name)) {
                    mo.putValue(name, props.get(name).asInt());
                } else if ("chac".equals(name)) {
                    // forward compatibility!
                    mo.putValue("rac", props.get(name).asInt());
                }
            }
            Iterator<JsonNode> it3 = member.get("cmpd_ids").elements();
            StringBuffer buf = new StringBuffer();
            int count = 0;
            while (it3.hasNext()) {
                count++;
                if (count > 1) {
                    buf.append(",");
                }
                JsonNode id = it3.next();
                buf.append(id.asText());
            }
            mo.putValue("cmpd_ids", buf.toString());

            mols.add(mo);
        }
        LOG.info("Generated " + mols.size() + " mols");
        return mols;
    }

    public static void main(String[] args) throws Exception {
        FragnetExpansionStep step = new FragnetExpansionStep();

        MoleculeObject mol = new MoleculeObject("c1ccc(Nc2nc3ccccc3o2)cc1", "smiles");
        List<MoleculeObject> mols = step.doGet(mol, 1, 3, 1);

//        MoleculeObject mol = new MoleculeObject("\n" +
//                "  Mrv1729 11011916322D          \n" +
//                "\n" +
//                "  7  7  0  0  0  0            999 V2000\n" +
//                "  -15.4464    5.9366    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
//                "  -16.1609    5.5241    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
//                "  -16.1609    4.6991    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
//                "  -15.4464    4.2866    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
//                "  -14.7320    4.6991    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
//                "  -14.7320    5.5241    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
//                "  -15.4464    6.7616    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
//                "  1  2  1  0  0  0  0\n" +
//                "  2  3  2  0  0  0  0\n" +
//                "  3  4  1  0  0  0  0\n" +
//                "  4  5  2  0  0  0  0\n" +
//                "  5  6  1  0  0  0  0\n" +
//                "  1  6  2  0  0  0  0\n" +
//                "  1  7  1  0  0  0  0\n" +
//                "M  END\n", "mol");
//        List<MoleculeObject> mols = step.doPost(mol, CommonMimeTypes.MIME_TYPE_MDL_MOLFILE, 1, 3, 1);

        System.out.println("Num results = " + mols.size());
        mols.forEach((mo) -> {
            System.out.println(mo);
        });

    }

}
