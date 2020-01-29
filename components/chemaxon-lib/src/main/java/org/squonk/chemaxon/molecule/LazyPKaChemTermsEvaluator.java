package org.squonk.chemaxon.molecule;

import chemaxon.nfunk.jep.ParseException;
import org.squonk.util.Metrics;

import java.util.Map;

import static org.squonk.util.Metrics.METRICS_PKA;
import static org.squonk.util.Metrics.PROVIDER_CHEMAXON;

public class LazyPKaChemTermsEvaluator extends LazyChemTermsEvaluator {

    private final String pkaTypePropName;

    /**
     *
     * @param pkaTypePropName The property name (the key in the Map) from the config that is passed in to the #doInit()
     *                        method that contains the type of pKa calculation. The value for that key in the Map must
     *                        be "acidic" or "basic"
     * @param resultPropName The name of the property to use to store the calculation result. If null then default names
     *                       are used.
     * @throws ParseException
     */
    public LazyPKaChemTermsEvaluator(String pkaTypePropName, String resultPropName) throws ParseException {
        super(resultPropName, null, Mode.Calculate, Metrics.generate(PROVIDER_CHEMAXON, METRICS_PKA));
        this.pkaTypePropName = pkaTypePropName;
    }

    public LazyPKaChemTermsEvaluator(String pkaTypePropName) throws ParseException {
        this(pkaTypePropName, null);
    }


    @Override
    protected MoleculeEvaluator doInit(Map<String, Object> config) {
        String pkaType = (String)config.get(pkaTypePropName);
        if (pkaType == null) {
            throw new IllegalStateException("Property named " + pkaTypePropName +
                    " for the type of pKa calculation must be defined in the config ");
        }
        try {
            String propName = getPropName();
            if ("acidic".equals(pkaType)) {
                return new ChemTermsEvaluator(
                        propName == null ? ChemTermsEvaluator.APKA : propName,
                        "acidicpKa('1')", getMetricsCode()
                );
            } else if ("basic".equals(pkaType)) {
                return new ChemTermsEvaluator(
                        propName == null ? ChemTermsEvaluator.BPKA : propName,
                        "basicpKa('1')", getMetricsCode()
                );
            }
        } catch (ParseException pe) {
            throw new RuntimeException("Failed to create ChemTermsEvaluator for pKa");
        }

        return null;
    }
}
