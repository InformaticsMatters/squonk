package com.im.lac.chemaxon.molecule;

import chemaxon.jep.ChemJEP;
import chemaxon.jep.Evaluator;
import chemaxon.jep.context.MolContext;
import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ChemTermsEvaluator {

    private final String propName;
    private final ChemJEP chemJEP;
    private final boolean filter;
    private final String chemTermsFunction;

    /**
     * Constructor to standard ChemTerms evaluator. THe property is calculated
     * and added to the molecule.
     *
     * @param propName The name of the resulting calculated property
     * @param chemTermsFunction Chemical terms expression
     * @throws ParseException
     */
    public ChemTermsEvaluator(String propName, String chemTermsFunction) throws ParseException {
        this(propName, chemTermsFunction, false);
    }

    /**
     * Constructor for a chemical terms filter. The expression must evaluate to
     * a boolean value.
     *
     * @param chemTermsFunction
     * @throws ParseException
     */
    public ChemTermsEvaluator(String chemTermsFunction) throws ParseException {
        this("filter", chemTermsFunction, true);
    }

    ChemTermsEvaluator(String propName, String chemTermsFunction, boolean isFilter) throws ParseException {
        this.propName = propName;
        this.chemTermsFunction = chemTermsFunction;
        this.filter = isFilter;
        // create ChemJEP, compile the Chemical Terms expression
        chemJEP = new Evaluator().compile(chemTermsFunction, MolContext.class);
    }

    public String getPropName() {
        return propName;
    }

    public String getChemTermsFunction() {
        return chemTermsFunction;
    }
    
    public boolean isFilter() {
        return filter;
    }

//    /**
//     * Evaluate a set of molecules. The result of the chemical terms evaluation
//     * is added as a property of each molecule
//     *
//     * @param mols
//     */
//    public void evaluateMolecules(Iterable<Molecule> mols) {
//        MolContext context = new MolContext();
//        for (Molecule mol : mols) {
//            context.setMolecule(mol);
//            evaluateMoleculeImpl(context);
//        }
//    }

    /**
     * Evaluate a single molecule. If this is a standard evaluator the result of 
     * the chemical terms evaluation is added as a property of the molecule.
     * If the evaluator is a filter then null is returned to signify that the filter
     * has failed
     *
     * @param mol The molecule.
     * @return The molecule with the calculated property set, or null if this evaluator 
     * is a filter and the filter fails.
     */
    public Molecule evaluateMolecule(Molecule mol) {
        MolContext context = new MolContext();
        context.setMolecule(mol);
        return evaluateMoleculeImpl(context);
    }

    private Molecule evaluateMoleculeImpl(MolContext context) {
        try {
            if (filter) {
                boolean b = chemJEP.evaluate_boolean(context);
                if (b) {
                    return context.getMolecule();
                }
            } else {
                Object result = chemJEP.evaluate(context);
                Molecule mol = context.getMolecule(); 
                mol.setPropertyObject(propName, result);
                return mol;
            }
        } catch (ParseException ex) {
            Logger.getLogger(ChemTermsEvaluator.class.getName())
                    .log(Level.WARNING, "Failed to evaluate chem terms expression", ex);
        }
        return null;
    }

    /**
     * Allows the result to be extracted from the Molecule once it has been
     * calculated
     *
     * @param mol
     * @return
     */
    public Object getResult(Molecule mol) {
        return mol.getPropertyObject(propName);
    }
}
