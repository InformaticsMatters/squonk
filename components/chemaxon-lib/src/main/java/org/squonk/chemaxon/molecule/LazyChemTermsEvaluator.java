package org.squonk.chemaxon.molecule;


import chemaxon.nfunk.jep.ParseException;

import java.util.Map;

public abstract class LazyChemTermsEvaluator extends ChemTermsEvaluator {


    public LazyChemTermsEvaluator(String propName, String chemTermsFunction, Mode mode, String metricsCode) throws ParseException {
        super(propName, chemTermsFunction, mode, metricsCode);
    }


    @Override
    public MoleculeEvaluator init(Map<String,Object> config) {
        return doInit(config);
    }

    protected abstract MoleculeEvaluator doInit(Map<String,Object> config);


}
