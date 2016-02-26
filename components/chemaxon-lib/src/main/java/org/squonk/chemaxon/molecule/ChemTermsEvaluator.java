package org.squonk.chemaxon.molecule;

import chemaxon.formats.MolFormatException;
import chemaxon.jep.ChemJEP;
import chemaxon.jep.Evaluator;
import chemaxon.jep.context.MolContext;
import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.Pool;
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
    private final ChemJEPPool pool;
    private final Mode mode;
    private final String chemTermsFunction;

    public static final String LOGP = "CXN_LogP";
    public static final String ATOM_COUNT = "CXN_AtomCount";
    public static final String BOND_COUNT = "CXN_BondCount";
    public static final String HBOND_ACCEPTOR_COUNT = "CXN_HBondAcceptorCount";
    public static final String HBOND_DONOR_COUNT = "CXN_HBondDonorCount";
    public static final String MOLECULAR_WEIGHT = "CXN_MolWeight";

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
        // create ChemJEP pool, compile the Chemical Terms expression
        this.pool = new ChemJEPPool(chemTermsFunction, 25);
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
        if (mol == null) {
            return null;
        }
        MolContext context = new MolContext();
        context.setMolecule(mol);
        return evaluateMoleculeImpl(context);
    }

    @Override
    public MoleculeObject processMoleculeObject(MoleculeObject mo) throws MolFormatException, IOException {
        if (mo == null || mo.getSource() == null) {
            return mo;
        }

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
        final ChemJEP chemJEP = pool.checkout();
        try {
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
                try {
                    Object result = chemJEP.evaluate(context);
                    context.getMolecule().setPropertyObject(propName, result);
                } catch (ParseException ex) {
                    LOG.log(Level.WARNING, "Failed to evaluate chem terms expression. Property will be missing.", ex);
                }
                return context.getMolecule();
            }
            return null;
        } finally {
            pool.checkin(chemJEP);
        }
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

    class ChemJEPPool extends Pool<ChemJEP> {

        final String chemTermsFunction;
        final Evaluator evaluator;

        ChemJEPPool(String chemTermsFunction, int size) throws ParseException {
            super(size);
            this.chemTermsFunction = chemTermsFunction;
            this.evaluator = new Evaluator();
            checkin(doCreate());
        }

        @Override
        protected ChemJEP create() {
            try {
                return doCreate();
            } catch (ParseException ex) {
                throw new RuntimeException("Failed to create ChemJEP", ex);
            }
        }

        private ChemJEP doCreate() throws ParseException {
            return evaluator.compile(chemTermsFunction, MolContext.class);
        }
    }
}
