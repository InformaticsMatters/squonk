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

package org.squonk.camel.chemaxon.processor;

import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.chemaxon.version.VersionInfo;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.squonk.chemaxon.molecule.ChemTermsEvaluator;
import org.squonk.chemaxon.molecule.MoleculeEvaluator;
import org.squonk.chemaxon.molecule.StandardizerEvaluator;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.Metrics;
import org.squonk.util.ResultExtractor;
import org.squonk.util.StatsRecorder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.squonk.util.Metrics.*;

/**
 * Processor that calculates chemical properties using ChemAxon's
 * <a href="https://docs.chemaxon.com/display/chemicalterms/Chemical+Terms+Home">
 * chemical terms</a> expressions and performs molecular transformations using
 * <a href="http://www.chemaxon.com/products/standardizer/">Standardizer</a>.
 * <br>
 * Three types of operation are supported:
 * <ol>
 * <li>Calculation of a molecular property using a Chemical Terms
 * expression</li>
 * <li>Filtering using a Chemical Terms expression</li>
 * <li>Transformation of the Molecule using a Chemical Terms expression</li>
 * <li>Transformation of the Molecule using a Standardizer</li>
 * </ol>
 * <p>
 * <p>
 * <b>Calculations</b>. The expressions can either be statically set using the
 * #calculate(String, String) method, of can be defined dynamically by setting a
 * header property. The property is added to the Molecule using the given
 * property name.
 * <p>
 * <p>
 * <b>Filtering</b>. This class can also be used to filter molecules. To do so
 * use the one-argument form of the constructor, or specify a filter in text
 * format using a syntax like this: filter=logP()&lt;5.
 * <br>
 * In both cases the chemical terms expression MUST evaluate to a boolean.
 * Multiple filter terms can be present. e.g.
 * filter=logP()&lt;5;filter=atomCount()&lt;30
 * <br>
 * Filtering only applies when the input (body of the Exchange) is a
 * Iterable&lt;Molecule&gt; or a Iterator&lt;Molecule&gt;. Only Molecules that
 * pass the filter are written to the output. When the input is a Molecule then
 * filtering is not applied as it is assumed to be more useful to just calculate
 * the properties and then inspect them for the single Molecule.
 * <br>The order of the filters will impact the performance, so consider this
 * carefully. Put your most selective filters first, and your slowest filters
 * last. You probably want to benchmark this if performance is a concern.
 * <p>
 * <p>
 * <b>Transforming</b>. Some chemical terms expressions generate structures. You
 * can utilise these to transform your input molecule. For instance you can use
 * the leconformer() expression to generate the lowest energy conformer of your
 * structure. To support this there is a "transform" mode that can be used to
 * replace your input molecule with the the result of the evaluation of the
 * chemical terms expression, with all properties of the source molecule being
 * copied to the new one.
 * <br>
 * To utilise this either use #transform(String) method to create the evaluator,
 * or use the "transform=chemTermsExpression()" syntax in an manner analogous to
 * defining filters.
 * <p>
 * <p>
 * <b>Standardizing</b>. The molecule can be standardized using a standardizer
 * configuration that is
 * <a href="https://docs.chemaxon.com/display/standardizer/Standardizer+Actions">
 * described here</a>. The action string syntax is the most suitable to use.
 * Note that there is also a separate {@link StandardizerProcessor} that is more
 * flexible in terms of configuration that should be used for more complex
 * scenarios.
 * <p>
 * <br>
 * <b>Headers</b>. If any expressions are set using the #calculate(string,
 * String) , #filter(String), #transform(String) or #standardize(String) methods
 * then header values are ignored. When none are set using these methods the
 * following header values are tried (the first one found is used):
 * <ul>
 * <li>The PROP_EVALUATORS_DEFINTION constant from this class</li>
 * <li>The org.apache.camel.Exchange.HTTP_QUERY constant, which is set by Camel
 * to the value of the query part of the URL</li>
 * </ul>
 * <br>
 * <b>Syntax</b>. The syntax of the header properties corresponds to the query
 * part of a URL. Multiple values can be separated with &amp; or ;. Examples
 * are:
 * <ul>
 * <li>logp=logP()&amp;atom_count=atomCount()</li>
 * <li>logd=logD('7.4');atom_count=atomCount()</li>
 * <li>filter=logP()&lt;5</li>
 * <li>standardize('addexplicith')</li>
 * </ul>
 * This allows the parameters to be specified as the query part of a URL.
 * e.g.<br>
 * http://some.server/path/to/resource?logp=logP();atom_count=atomCount()
 * <br>
 * NOTE: if using this approach the URL almost certainly needs to be URL
 * encoded.
 * <br>
 * <b>Performance</b>. Expressions defined using the header property are created
 * and used ONLY for that one Exchange, so this is not an efficient approach for
 * multiple Exchanges. Instead you should pack your Molecules up as an
 * Iterable&lt;Molecule&gt; or Iterator&lt;Molecule&gt; and process them all in
 * one Exchange.
 * <p>
 * <p>
 * <b>Accepted molecule formats</b>. The following inputs are supported (tried
 * in this order) with the corresponding outputs
 * <ol>
 * <li>Input: Molecule Output: Molecule</li>
 * <li>Input: Iterable&lt;Molecule&gt; Output: Iterable&lt;Molecule&gt;</li>
 * </ol>
 *
 * @author Tim Dudgeon
 */
public class ChemAxonMoleculeProcessor implements Processor, ResultExtractor<Molecule> {

    private static final Logger LOG = Logger.getLogger(ChemAxonMoleculeProcessor.class.getName());
    public static final String PROP_EVALUATORS_DEFINTION = "ChemTermsProcessor_EvaluatorsDefintion";

    private boolean sequential = false;

    private final List<MoleculeEvaluator> evaluators = new ArrayList<>();

    public boolean isSequential() {
        return sequential;
    }

    public void makeSequential() {
        this.sequential = true;
    }

    /**
     * Add a new calculation using a chemical terms expression. If no terms are
     * added then the header values are tried for the definitions.
     * <br>
     * Note: the return type is the instance, to allow the fluent builder
     * pattern to be used.
     *
     * @param name         The name for the calculated property
     * @param ctExpression The chemical terms expression e.g. logP()
     * @return
     * @throws ParseException
     */
    public ChemAxonMoleculeProcessor calculate(String name, String ctExpression, String metricsCode) throws ParseException {
        evaluators.add(new ChemTermsEvaluator(name, ctExpression, metricsCode));
        return this;
    }

    /**
     * Create a new filter based on a chemical terms expression. The expression
     * MUST evaluate to a boolean value. e.g. logP() &lt; 5
     *
     * @param ctExpression
     * @return
     * @throws ParseException
     */
    public ChemAxonMoleculeProcessor filter(String ctExpression, String metricsCode) throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ctExpression, ChemTermsEvaluator.Mode.Filter, metricsCode));
        return this;
    }



    /**
     * Add a transform definition which replaces the input molecule with one
     * generated from it using the specified chemical terms expression. The
     * chemical terms expression MUST return a molecule. All SD file properties
     * are copies from the source to the new Molecule.
     *
     * @param ctExpression the expression which MUST return a Molecule
     * @return
     * @throws ParseException
     */
    public ChemAxonMoleculeProcessor transform(String ctExpression, String metricsCode) throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ctExpression, ChemTermsEvaluator.Mode.Transform, metricsCode));
        return this;
    }

    public ChemAxonMoleculeProcessor standardize(String szrExpression) {
        evaluators.add(new StandardizerEvaluator(szrExpression, 25));
        return this;
    }

    @Override
    public void process(final Exchange exch) throws Exception {
        final List<MoleculeEvaluator> evals = createEvaluators(exch);

        Dataset dataset = exch.getIn().getBody(Dataset.class);
        if (dataset == null || dataset.getType() != MoleculeObject.class) {
            throw new IllegalStateException("Input must be a Dataset of MoleculeObjects");
        }
        Stream<MoleculeObject> mols = dataset.getStream();
        if (sequential) {
            mols.sequential();
        } else {
            mols.parallel();
        }

        Map<String,Integer> stats = new HashMap<>();
        for (MoleculeEvaluator eval : evals) {
            mols = calculateMultiple(mols, eval, stats);
        }
        StatsRecorder recorder = exch.getIn().getHeader(StatsRecorder.HEADER_STATS_RECORDER, StatsRecorder.class);
        if (recorder != null) {
            mols = mols.onClose(() -> {
                recorder.recordStats(stats);
            });
        }
        DatasetMetadata meta = handleMetadata(exch, dataset.getMetadata(), evals);
        LOG.info("Generated metadata: " + JsonHandler.getInstance().objectToJson(meta));
        exch.getIn().setBody(new MoleculeObjectDataset(mols, meta));
    }

    protected DatasetMetadata handleMetadata(Exchange exch, DatasetMetadata meta, List<MoleculeEvaluator> evals) {
        if (meta == null) {
            meta = new DatasetMetadata(MoleculeObject.class);
        }
        String source = "JChem " + VersionInfo.getVersion();
        for (MoleculeEvaluator eval : evals) {
            String fieldName = eval.getPropName();
            if (fieldName == null) {
                // a filter or transform
                meta.appendDatasetHistory(eval.getDescription() + "[" + source + "]");
            } else {
                meta.createField(fieldName, source, eval.getDescription(), null);
            }
        }
        return meta;
    }


    private Stream<MoleculeObject> calculateMultiple(Stream<MoleculeObject> input, MoleculeEvaluator evaluator, Map<String,Integer> stats) throws Exception {
        //LOG.info("Calculating for stream " + input);

        Stream<MoleculeObject> result = input;
        //LOG.log(Level.INFO, "Handling evaluator {0}", evaluator);
        AtomicInteger count = new AtomicInteger(0);
        switch (evaluator.getMode()) {
            case Filter:
                result = result.filter((mo) -> {
                    int i = count.incrementAndGet();
                    if (i % 5000 == 0) {
                        LOG.info("Processed molecule " + i + " " + Thread.currentThread());
                    }
                    try {
                        boolean b = evaluator.processMoleculeObject(mo, stats) != null;
                        return b;
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Failed to evaluate molecule", ex);
                    }
                    return false;
                });
                break;
            default:
                result = result.map((mo) -> {
                    int i = count.incrementAndGet();
                    if (i % 5000 == 0) {
                        LOG.info("Processed molecule " + i + " " + Thread.currentThread());
                    }
                    try {
                        MoleculeObject r = evaluator.processMoleculeObject(mo, stats);
                        return r;
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Failed to evaluate molecule", ex);
                    }
                    return mo;
                });
        }
        return result;
    }


    /**
     * Get or create the evaluators by whatever means we can. If these have been
     * defined using the #calculate() or related methods then those are used. If
     * not we try to create ones specific for this Exchange based on header
     * parameters.
     *
     * @param exchange
     * @return
     * @throws ParseException
     */
    List<MoleculeEvaluator> createEvaluators(Exchange exchange) throws ParseException {
        List<MoleculeEvaluator> result = null;
        if (evaluators != null && !evaluators.isEmpty()) {
            result = evaluators;
        } else {
            String defs = exchange.getIn().getHeader(PROP_EVALUATORS_DEFINTION, String.class);
            if (defs == null) {
                defs = exchange.getIn().getHeader(Exchange.HTTP_QUERY, String.class);
            }
            if (defs != null) {
                LOG.finer("Generating dynamic ChemTerms evaluators");
                result = parseParamString(defs);
            } else {
                throw new IllegalStateException("No Chem terms configuration supplied");
            }
        }
        return result;
    }

    /**
     * Get the calculated results for the Molecule. Allow for a molecule to be
     * passed back
     *
     * @param mol
     * @return
     */
    @Override
    public Map<String, Object> extractResults(Molecule mol
    ) {
        Map<String, Object> results = new HashMap<>();
        for (MoleculeEvaluator evaluator : evaluators) {
            Map<String, Object> data = evaluator.getResults(mol);
            results.putAll(data);
        }
        return results;
    }

    static List<MoleculeEvaluator> parseParamString(String query) throws ParseException {
        List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
        List<MoleculeEvaluator> evals = new ArrayList<>();
        for (NameValuePair nvp : params) {
            if (null != nvp.getName()) {
                switch (nvp.getName()) {
                    case "filter":
                        evals.add(new ChemTermsEvaluator(nvp.getValue(), ChemTermsEvaluator.Mode.Filter, null));
                        break;
                    case "transform":
                        evals.add(new ChemTermsEvaluator(nvp.getValue(), ChemTermsEvaluator.Mode.Transform, null));
                        break;
                    case "standardize":
                        evals.add(new StandardizerEvaluator(nvp.getValue(), 25));
                        break;
                    default:
                        evals.add(new ChemTermsEvaluator(nvp.getName(), nvp.getValue(), null));
                        break;
                }
            }
        }
        return evals;
    }


    public ChemAxonMoleculeProcessor logP() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.LOGP, "logP()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_LOGP)));
        return this;
    }

    public ChemAxonMoleculeProcessor logD(Float pH) throws ParseException {
        String pHString = pH == null ? "7.4" : pH.toString();
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.LOGD + "_" + pHString, "logD('" + pHString + "')", Metrics.generate(PROVIDER_CHEMAXON, METRICS_LOGD)));
        return this;
    }

    public ChemAxonMoleculeProcessor logS(Float pH, String result) throws ParseException {
        String pHString = pH == null ? "7.4" : pH.toString();
        String resultString = result == null ? "logs" : result;
        // result type does not seem to be handled by this version of JChem
//        String propName = ChemTermsEvaluator.LOGS + "_" + pHString + "_" + resultString;
//        String ctExpr = "logS('" + pHString + "','" + resultString + "')";
        String propName = ChemTermsEvaluator.LOGS + "_" + pHString;
        String ctExpr = "logS('" + pHString + "')";
        evaluators.add(new ChemTermsEvaluator(propName, ctExpr, Metrics.generate(PROVIDER_CHEMAXON, METRICS_LOGS)));
        // The logS predictor does not run correctly. This workaround of avoiding multi-threading in place until it is resolved.
        // See https://github.com/InformaticsMatters/squonk/issues/13
        this.makeSequential();
        return this;
    }

    public ChemAxonMoleculeProcessor donorCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.HBOND_DONOR_COUNT, "donorCount()",Metrics.generate(PROVIDER_CHEMAXON, METRICS_HBD)));
        return this;
    }

    public ChemAxonMoleculeProcessor acceptorCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT, "acceptorCount()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_HBA)));
        return this;
    }

    public ChemAxonMoleculeProcessor atomCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.ATOM_COUNT, "atomCount()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_ATOM_COUNT)));
        return this;
    }

    /**
     *
     * @param expression The expression for the atomCount(XXX) function e.g. "6" to count the carbon atoms.
     * @param propertyName The name for the generated property.
     * @return
     * @throws ParseException
     */
    public ChemAxonMoleculeProcessor atomCount(String expression, String propertyName) throws ParseException {
        evaluators.add(new ChemTermsEvaluator(propertyName, "atomCount('" + expression + "')", Metrics.generate(PROVIDER_CHEMAXON, METRICS_ATOM_COUNT)));
        return this;
    }

    public ChemAxonMoleculeProcessor heavyAtomCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.HEAVY_ATOM_COUNT, "atomCount() - atomCount('1')", Metrics.generate(PROVIDER_CHEMAXON, METRICS_ATOM_COUNT)));
        return this;
    }

    public ChemAxonMoleculeProcessor bondCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.BOND_COUNT, "bondCount()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_BOND_COUNT)));
        return this;
    }

    public ChemAxonMoleculeProcessor molWeight() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.MOLECULAR_WEIGHT, "mass()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_MASS)));
        return this;
    }

    public ChemAxonMoleculeProcessor ringCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.RING_COUNT, "ringCount()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_RING_COUNT)));
        return this;
    }

    public ChemAxonMoleculeProcessor aromaticRingCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.AROMATIC_RING_COUNT, "aromaticRingCount()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_RING_COUNT)));
        return this;
    }

    public ChemAxonMoleculeProcessor rotatableBondCount() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.ROTATABLE_BOND_COUNT, "rotatableBondCount()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_ROTATABLE_BOND_COUNT)));
        return this;
    }

    public ChemAxonMoleculeProcessor molarRefractivity() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.MOLAR_REFRACTIVITY, "refractivity()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_MOLAR_REFRACTIVITY)));
        return this;
    }

    public ChemAxonMoleculeProcessor formalCharge() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.FORMAL_CHARGE, "formalCharge()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_CHARGE)));
        return this;
    }

    public ChemAxonMoleculeProcessor tpsa() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.TPSA, "topologicalPolarSurfaceArea()", Metrics.generate(PROVIDER_CHEMAXON, METRICS_PSA)));
        return this;
    }

    public ChemAxonMoleculeProcessor apKa() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.APKA, "acidicpKa('1')", Metrics.generate(PROVIDER_CHEMAXON, METRICS_PKA)));
        return this;
    }

    public ChemAxonMoleculeProcessor bpKa() throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ChemTermsEvaluator.BPKA, "basicpKa('1')", Metrics.generate(PROVIDER_CHEMAXON, METRICS_PKA)));
        return this;
    }

}
