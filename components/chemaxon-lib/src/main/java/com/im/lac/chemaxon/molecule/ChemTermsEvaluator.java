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

    private final ChemJEP chemJEP;
    private final String propName;

    /**
     *
     * @param chemTermsFunction Chemical terms expression
     * @param propName The name of the resulting calculated property
     * @throws ParseException
     */
    public ChemTermsEvaluator(String chemTermsFunction, String propName) throws ParseException {
        // create ChemJEP, compile the Chemical Terms expression
        chemJEP = new Evaluator().compile(chemTermsFunction, MolContext.class);
        this.propName = propName;
    }

    public String getPropName() {
        return propName;
    }

    /**
     * Evaluate a set of molecules. The result of the chemical terms evaluation
     * is added as a property of each molecule
     *
     * @param mols
     */
    public void evaluateMolecules(Iterable<Molecule> mols) {
        MolContext context = new MolContext();
        for (Molecule mol : mols) {
            context.setMolecule(mol);
            evaluateMoleculeImpl(context);
        }
    }

    /**
     * Evaluate a single molecule. If you have multiple molecules then the
     * #evaluateMolecules(Iterable<Molecule> mols) method will be more
     * efficient. The result of the chemical terms evaluation is added as a
     * property of the molecule
     *
     * @param mol The molecule.
     */
    public void evaluateMolecule(Molecule mol) {
        MolContext context = new MolContext();
        context.setMolecule(mol);
        evaluateMoleculeImpl(context);
    }

    private void evaluateMoleculeImpl(MolContext context) {
        try {
            Object result = chemJEP.evaluate(context);
            context.getMolecule().setPropertyObject(propName, result);
        } catch (ParseException ex) {
            Logger.getLogger(ChemTermsEvaluator.class.getName())
                    .log(Level.WARNING, "Failed to evaluate chem terms expression", ex);
        }
    }

    /**
     * Allows the result to be extracted from the Molecule once it has been calculated
     *
     * @param mol
     * @return
     */
    public Object getResult(Molecule mol) {
        return mol.getPropertyObject(propName);
    }
}
