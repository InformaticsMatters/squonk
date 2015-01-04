package com.im.lac.camel.chemaxon.processor;

import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.im.lac.ClosableQueue;
import com.im.lac.ResultExtractor;
import com.im.lac.chemaxon.molecule.ChemTermsEvaluator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import java.util.logging.Logger;

/**
 * Processor that calculates chemical properties using ChemAxon's
 * <a href="https://docs.chemaxon.com/display/chemicalterms/Chemical+Terms+Home">
 * chemical terms</a> expressions.
 * <br>
 * The expressions can either be statically set using the #add(String, String)
 * method, of can be defined dynamically by setting the
 * PROP_EVALUATORS_DEFINTION header property to a Map (key is the property name,
 * value is the chemical terms expression). If any expressions are set using the
 * #add(string, String) method then the header value is ignored.
 * <br>
 * Expressions defined using the PROP_EVALUATORS_DEFINTION header property are
 * created and used only for that one Exchange, so this is not an efficient
 * approach for multiple Exchanges. Instead you should pack your Molecules up as
 * an Iterable&lt;Molecule&gt; or Iterator&lt;Molecule&gt; and process them all
 * in one go.
 * <br>
 * The following inputs are supported (tried in this order) with the corresponding
 * outputs
 * <table cellPadding="3">
 * <tr><th>Input</th><th>Output</th></tr>
 * <tr><td>Molecule</td><td>Molecule</td></tr>
 * <tr><td>Iterator&lt;Molecule&gt;</td><td>Iterable&lt;Molecule&gt;</td></tr>
 * <tr><td>Iterable&lt;Molecule&gt;</td><td>Iterable&lt;Molecule&gt;</td></tr>
 * </table>
 *
 * @author Tim Dudgeon
 */
public class ChemTermsProcessor implements Processor, ResultExtractor<Molecule> {

    private static final Logger LOG = Logger.getLogger(ChemTermsProcessor.class.getName());
    public static final String PROP_EVALUATORS_DEFINTION = "ChemTermsProcessor_EvaluatorsDefintion";

    private final List<ChemTermsEvaluator> evaluators = new ArrayList<ChemTermsEvaluator>();

    /**
     * Add a new chemical terms expression. If no terms are added then the
     * PROP_EVALUATORS_DEFINTION header is used for the definitions.
     *
     * @param ctExpression The chemical terms expression e.g. logP()
     * @param name The name for the calculated property
     * @return
     * @throws ParseException
     */
    public ChemTermsProcessor add(String ctExpression, String name) throws ParseException {

        evaluators.add(new ChemTermsEvaluator(ctExpression, name));
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
                final Iterator<Molecule> mols = iterator;
                final ClosableQueue q = new ClosableQueue(100);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (mols.hasNext()) {
                                Molecule mol = mols.next();
                                LOG.finer("Processing Molecule");
                                for (ChemTermsEvaluator evaluator : evals) {
                                    evaluator.evaluateMolecule(mol);
                                }
                                q.add(mol);
                            }
                        } finally {
                            q.close();
                        }
                    }
                };
                t.start();
                exchange.getIn().setBody(q);
            } else {
                // give up
                Object body = exchange.getIn().getBody();
                LOG.log(Level.WARNING, "Can''t find molecules from {0}", body.getClass().getName());
                throw new IllegalArgumentException("No valid Molecule content could be found");
            }
        }
    }

    private List<ChemTermsEvaluator> getEvaluators(Exchange exchange) throws ParseException {
        List<ChemTermsEvaluator> result = null;
        if (evaluators != null && !evaluators.isEmpty()) {
            result = evaluators;
        } else {
            Map<String, String> defs = exchange.getIn().getHeader(PROP_EVALUATORS_DEFINTION, Map.class);
            LOG.finer("Generating dynamic ChemTerms evaluators");
            if (defs != null) {
                result = new ArrayList<ChemTermsEvaluator>();
                for (Map.Entry<String, String> e : defs.entrySet()) {
                    LOG.log(Level.FINE, "Adding CT Evaluator for {0} -> {1}", new Object[]{e.getKey(), e.getValue()});
                    result.add(new ChemTermsEvaluator(e.getValue(), e.getKey()));
                }
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
}
