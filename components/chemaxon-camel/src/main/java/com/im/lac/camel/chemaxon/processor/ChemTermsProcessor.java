package com.im.lac.camel.chemaxon.processor;

import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.im.lac.ClosableQueue;
import com.im.lac.ResultExtractor;
import com.im.lac.chemaxon.molecule.ChemTermsEvaluator;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Processor that calculates chemical properties using ChemAxon's
 * <a href="https://docs.chemaxon.com/display/chemicalterms/Chemical+Terms+Home">
 * chemical terms</a> expressions.
 * <br>
 * The expressions can either be statically set using the #add(String, String)
 * method, of can be defined dynamically by setting a header property. If any
 * expressions are set using the #add(string, String) method then the header
 * value is ignored.
 * <br>
 * <b>Headers</b>. For chemical terms expressions using header values, the
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
 * <br>
 * <b>Filtering</b>. This class can also be used to filter molecules. To do so use
 * the one-argument form of the constructor, or specify a filter in text format using 
 * a syntax like this: filter=logP()&lt;5.
 * <br>
 * In both cases the chemical terms expression MUST evaluate to a boolean. 
 * Multiple filter terms can be present. e.g.
 * filter=logP()&lt;5;filter=atomCount()&lt;30
 * <br>
 * Filtering only applies when the input (body of the Exchange) is a Iterable&lt;Molecule&gt; 
 * or a Iterator&lt;Molecule&gt;. Only Molecules that pass the filter are written to the output.
 * When the input is a Molecule then filtering is not applied as it is assumed to be
 * more useful to just calculate the properties and then inspect them for the single Molecule.
 * <br>The order of the filters will impact the performance, so consider this carefully.
 * Put your most selective filters first, and your slowest filters last. You probably
 * want to benchmark this if performance is a concern.
 * <br>
 * <b>Accepted molecule formats</b>. The following inputs are supported (tried
 * in this order) with the corresponding outputs
 * <ol>
 * <li>Input: Molecule Output: Molecule</li>
 * <li>Input: Iterator&lt;Molecule&gt; Output: Iterable&lt;Molecule&gt;</li>
 * <li>Input: Iterable&lt;Molecule&gt; Output: Iterable&lt;Molecule&gt;</li>
 * </ol>
 *
 * @author Tim Dudgeon
 */
public class ChemTermsProcessor implements Processor, ResultExtractor<Molecule> {

    private static final Logger LOG = Logger.getLogger(ChemTermsProcessor.class.getName());
    public static final String PROP_EVALUATORS_DEFINTION = "ChemTermsProcessor_EvaluatorsDefintion";

    private final List<ChemTermsEvaluator> evaluators = new ArrayList<ChemTermsEvaluator>();

    /**
     * Add a new chemical terms expression. If no terms are added then the
     * header values are tried for the definitions.
     * <br>
     * Note: the return type is the instance, to allow the fluent builder
     * pattern to be used.
     *
     * @param name The name for the calculated property
     * @param ctExpression The chemical terms expression e.g. logP()
     * @return
     * @throws ParseException
     */
    public ChemTermsProcessor add(String name, String ctExpression) throws ParseException {
        evaluators.add(new ChemTermsEvaluator(name, ctExpression));
        return this;
    }

    /** Create a new filter based on a chemical terms expression.
     * The expression MUST evaluate to a boolean value. e.g. logP() &lt; 5
     * 
     * @param ctExpression
     * @return
     * @throws ParseException 
     */
    public ChemTermsProcessor filter(String ctExpression) throws ParseException {
        evaluators.add(new ChemTermsEvaluator(ctExpression));
        return this;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LOG.fine("Processing ChemTerms");
        final List<ChemTermsEvaluator> evals = getEvaluators(exchange);
        // first try as molecule
        Molecule mol = exchange.getIn().getBody(Molecule.class);
        if (mol != null) {
            for (ChemTermsEvaluator evaluator : evals) {
                evaluator.evaluateMolecule(mol);
            }
            exchange.getIn().setBody(mol);
        } else {
            // try as stream of molecules
            Iterator<Molecule> iterator = exchange.getIn().getBody(Iterator.class);
            if (iterator == null) {
                Iterable<Molecule> iterable = exchange.getIn().getBody(Iterable.class);
                if (iterable != null) {
                    iterator = iterable.iterator();
                }
            }

            if (iterator != null) {
                Iterator<Molecule> mols = iterator;
                for (ChemTermsEvaluator evaluator : evals) {
                    mols = evaluateMultiple(mols, evaluator);
                }
                exchange.getIn().setBody(mols);
            } else {
                // give up
                Object body = exchange.getIn().getBody();
                LOG.log(Level.WARNING, "Can''t find molecules from {0}", body.getClass().getName());
                throw new IllegalArgumentException("No valid Molecule content could be found");
            }
        }
    }

    ClosableQueue<Molecule> evaluateMultiple(final Iterator<Molecule> mols, final ChemTermsEvaluator evaluator) {
        final ClosableQueue<Molecule> q = new ClosableQueue<Molecule>(50);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (mols.hasNext()) {
                        Molecule mol = mols.next();
                        synchronized (evaluator) { // chem terms evaluator is not thread safe
                            mol = evaluator.evaluateMolecule(mol);
                        }
                        if (mol != null) {
                            q.add(mol);
                        }
                    }
                } finally {
                    q.close();
                }
            }
        });
        t.start();

        return q;
    }

    /**
     * Get or create the evaluators by whatever means we can. If these have been
     * defined using the #add() method then those are used. If not we try to
     * create ones specific for this Exchange based on header parameters.
     *
     * @param exchange
     * @return
     * @throws ParseException
     */
    List<ChemTermsEvaluator> getEvaluators(Exchange exchange) throws ParseException {
        List<ChemTermsEvaluator> result = null;
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
    public Map<String, Object> extractResults(Molecule mol) {
        Map<String, Object> results = new HashMap<String, Object>();
        for (ChemTermsEvaluator evaluator : evaluators) {
            results.put(evaluator.getPropName(), evaluator.getResult(mol));
        }
        return results;
    }

    static List<ChemTermsEvaluator> parseParamString(String query) throws ParseException {
        List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
        List<ChemTermsEvaluator> evals = new ArrayList<ChemTermsEvaluator>();
        for (NameValuePair nvp : params) {
            if ("filter".equals(nvp.getName())) {
                evals.add(new ChemTermsEvaluator(nvp.getValue()));
            } else {
                evals.add(new ChemTermsEvaluator(nvp.getName(), nvp.getValue()));
            }
        }
        return evals;
    }
}
