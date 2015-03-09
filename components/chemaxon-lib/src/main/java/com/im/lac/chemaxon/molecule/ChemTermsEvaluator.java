package com.im.lac.chemaxon.molecule;

import chemaxon.formats.MolFormatException;
import chemaxon.jep.ChemJEP;
import chemaxon.jep.Evaluator;
import chemaxon.jep.context.MolContext;
import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class ChemTermsEvaluator implements MoleculeEvaluator {

    private static final Logger LOG = Logger.getLogger(ChemTermsEvaluator.class.getName());
    private final String propName;
    private final ChemJEP chemJEP;
    private final Mode mode;
    private final String chemTermsFunction;

    /**
     * Constructor to standard ChemTerms evaluator. The property is calculated
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

    @Override
    public Mode getMode() {
        return mode;
    }

    /**
     * Process a molecule. If this is a standard evaluator the result of the
     * chemical terms evaluation is added as a property of the molecule. If the
     * evaluator is a filter then null is returned to signify that the filter
     * has failed
     *
     * @param mol The molecule.
     * @return The molecule with the calculated property set, or null if this
     * evaluator is a filter and the filter fails.
     */
    @Override
    public Molecule processMolecule(Molecule mol) {
        MolContext context = new MolContext();
        context.setMolecule(mol);
        synchronized (chemJEP) { // not thread safe
            return evaluateMoleculeImpl(context);
        }
    }

    @Override
    public MoleculeObject processMoleculeObject(MoleculeObject mo) throws MolFormatException, IOException {
        Molecule mol = MoleculeUtils.fetchMolecule(mo, true);
        mol = processMolecule(mol);

        if (mol == null) {
            return null;
        } else {
            Map<String, Object> results = getResults(mol);
            MoleculeObject neu = MoleculeUtils.derriveMoleculeObject(mo, mol, mo.getFormat("mol"));
            neu.putValues(results);
            return neu;
        }
    }

    private Molecule evaluateMoleculeImpl(MolContext context) {

        if (mode == Mode.Filter) {
            try {
                boolean b = chemJEP.evaluate_boolean(context);
                if (b) {
                    return context.getMolecule();
                }
            } catch (ParseException ex) {
                LOG.log(Level.WARNING, "Failed to evaluate chem terms expression. Molecule is filtered out.", ex);
            }

        } else if (mode == Mode.Transform) {
            Molecule oldMol = context.getMolecule();
            try {
                Molecule newMol = (Molecule) chemJEP.evaluate(context);
                newMol.clearProperties();
                for (int i = 0; i < oldMol.getPropertyCount(); i++) {
                    String key = oldMol.getPropertyKey(i);
                    Object val = oldMol.getPropertyObject(key);
                    newMol.setPropertyObject(key, val);
                }
                return newMol;
            } catch (ParseException ex) {
                LOG.log(Level.WARNING, "Failed to evaluate chem terms expression. Molecule is excluded.", ex);
            }
        } else if (mode == Mode.Calculate) {
            Molecule mol = context.getMolecule();
            try {
                Object result = chemJEP.evaluate(context);
                mol.setPropertyObject(propName, result);
            } catch (ParseException ex) {
                LOG.log(Level.WARNING, "Failed to evaluate chem terms expression. Property will be missing.", ex);
            }
            return mol;
        }
        return null;
    }

    /**
     * Allows the result to be extracted from the Molecule once it has been
     * calculated. If not Mode.Calculate an empty Map is returned
     *
     * @param mol
     * @return the calculated value as a Map, key being the property name.
     */
    @Override
    public Map<String, Object> getResults(Molecule mol) {
        if (mode == Mode.Calculate) {
            Object value = mol.getPropertyObject(propName);
            return Collections.singletonMap(propName, value);
        } else {
            return Collections.emptyMap();
        }
    }
}
