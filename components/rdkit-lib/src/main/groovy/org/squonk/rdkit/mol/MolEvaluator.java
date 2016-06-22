package org.squonk.rdkit.mol;

import org.squonk.types.MoleculeObject;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class MolEvaluator {

    static {
        System.loadLibrary("GraphMolWrap");
    }

    private static final Logger LOG = Logger.getLogger(MolEvaluator.class.getName());

    public static void evaluate(MoleculeObject mo, ROMol rdkitMol, EvaluatorDefintion definition) {
        if (mo.getSource() != null || definition == null) {
            LOG.log(Level.FINER, "Evaluating {0}", definition);
            Object result = calculate(rdkitMol, definition.function);
            switch (definition.mode) {
                case Calculate:
                    mo.putValue(definition.propName, result);
                    break;
                case Filter:
                    throw new UnsupportedOperationException("NYI");
            }
        }
    }

    public static Object calculate(ROMol rdkitMol, EvaluatorDefintion.Function function) {

        if (rdkitMol == null || function == null) {
            return null;
        }

        switch (function) {
            case LOGP:
                return cooerceFloat(RDKFuncs.calcMolLogP(rdkitMol));
            case EXACT_MW:
                return cooerceFloat(RDKFuncs.calcExactMW(rdkitMol));
            case FRACTION_C_SP3:
                return cooerceFloat(RDKFuncs.calcFractionCSP3(rdkitMol));
            case NUM_HBA:
                return cooerceInt(RDKFuncs.calcNumHBA(rdkitMol));
            case NUM_HBD:
                return cooerceInt(RDKFuncs.calcNumHBD(rdkitMol));
            case LIPINSKI_HBA:
                return cooerceInt(RDKFuncs.calcLipinskiHBA(rdkitMol));
            case LIPINSKI_HBD:
                return cooerceInt(RDKFuncs.calcLipinskiHBD(rdkitMol));
            case MOLECULAR_FORMULA:
                return RDKFuncs.calcMolFormula(rdkitMol);
            case MOLAR_REFRACTIVITY:
                return cooerceFloat(RDKFuncs.calcMolMR(rdkitMol));
            case NUM_HETEROATOMS:
                return cooerceInt(RDKFuncs.calcNumHeteroatoms(rdkitMol));
            case NUM_RINGS:
                return cooerceInt(RDKFuncs.calcNumRings(rdkitMol));
            case NUM_AROMATIC_RINGS:
                return cooerceInt(RDKFuncs.calcNumAromaticRings(rdkitMol));
            case NUM_ROTATABLE_BONDS:
                return cooerceInt(RDKFuncs.calcNumRotatableBonds(rdkitMol));
            case TPSA:
                return cooerceFloat(RDKFuncs.calcTPSA(rdkitMol));
            case BOND_COUNT:
                return cooerceInt(rdkitMol.getNumBonds());
            case ATOM_COUNT:
                return cooerceInt(rdkitMol.getNumAtoms());
            case HEAVY_ATOM_COUNT:
                return cooerceInt(rdkitMol.getNumHeavyAtoms());
            case FORMAL_CHARGE:
                return cooerceInt(RDKFuncs.getFormalCharge(rdkitMol));
        }
        LOG.log(Level.WARNING, "Function {0} not handled", function);
        return null;
    }

    private static Integer cooerceInt(Number n) {
        if (n == null) {
            return null;
        } else {
            return n.intValue();
        }
    }

    private static Float cooerceFloat(Number n) {
        if (n == null) {
            return null;
        } else {
            return n.floatValue();
        }
    }
}
