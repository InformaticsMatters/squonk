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
    
    public enum Mode { Calculate, Filter, Transform }

    private final String propName;
    private final ChemJEP chemJEP;
    private final Mode mode;
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
        this(propName, chemTermsFunction, Mode.Calculate);
    }

    /**
     * Constructor for a chemical terms filter. The expression must evaluate to
     * a boolean value.
     *
     * @param chemTermsFunction
     * @param mode Should be Filter or Transform
     * @throws ParseException
     */
    public ChemTermsEvaluator(String chemTermsFunction, Mode mode) throws ParseException {
        this("filter", chemTermsFunction, mode);
    }

    ChemTermsEvaluator(String propName, String chemTermsFunction, Mode mode) throws ParseException {
        this.propName = propName;
        this.chemTermsFunction = chemTermsFunction;
        this.mode = mode;
        // create ChemJEP, compile the Chemical Terms expression
        chemJEP = new Evaluator().compile(chemTermsFunction, MolContext.class);
    }

    public String getPropName() {
        return propName;
    }

    public String getChemTermsFunction() {
        return chemTermsFunction;
    }
    
    public Mode getMode() {
        return mode;
    }

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
            if (mode == Mode.Filter) {
                boolean b = chemJEP.evaluate_boolean(context);
                if (b) {
                    return context.getMolecule();
                }
            } else if (mode == Mode.Transform) {
                Molecule oldMol = context.getMolecule();
                Molecule newMol = (Molecule)chemJEP.evaluate(context);
                newMol.clearProperties();
                for (int i = 0; i < oldMol.getPropertyCount(); i++) {
                    String key = oldMol.getPropertyKey(i);
                    Object val = oldMol.getPropertyObject(key);
                    newMol.setPropertyObject(key, val);
                }
                return newMol;
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
