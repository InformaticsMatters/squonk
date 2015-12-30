package org.squonk.rdkit.mol;

import com.im.lac.types.MoleculeObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;

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
        LOG.log(Level.FINER, "Evaluating {0}", definition);
        EvaluatorDefintion.Function func = EvaluatorDefintion.Function.valueOf(definition.expression);
        Object result = calculate(rdkitMol, func);
        switch (definition.mode) {
            case Calculate:
                mo.putValue(definition.propName, result);
                break;
            case Filter:
                throw new UnsupportedOperationException("NYI");
        }
    }

    public static Object calculate(ROMol rdkitMol, EvaluatorDefintion.Function function) {

        switch (function) {
            case LOGP:
                return RDKFuncs.calcMolLogP(rdkitMol);
            case EXACT_MW:
                return RDKFuncs.calcExactMW(rdkitMol);
            case FRACTION_C_SP3:
                return RDKFuncs.calcFractionCSP3(rdkitMol);
            case NUM_HBA:
                return RDKFuncs.calcNumHBA(rdkitMol);
            case NUM_HBD:
                return RDKFuncs.calcNumHBD(rdkitMol);
            case LIPINSKI_HBA:
                return RDKFuncs.calcLipinskiHBA(rdkitMol);
            case LIPINSKI_HBD:
                return RDKFuncs.calcLipinskiHBD(rdkitMol);
            case MOLECULAR_FORMULA:
                return RDKFuncs.calcMolFormula(rdkitMol);
            case MOLAR_REFRACTIVITY:
                return RDKFuncs.calcMolMR(rdkitMol);
            case NUM_HETEROATOMS:
                return RDKFuncs.calcNumHeteroatoms(rdkitMol);
            case NUM_RINGS:
                return RDKFuncs.calcNumRings(rdkitMol);
            case NUM_AROMATIC_RINGS:
                return RDKFuncs.calcNumAromaticRings(rdkitMol);
            case NUM_ROTATABLE_BONDS:
                return RDKFuncs.calcNumRotatableBonds(rdkitMol);
            case TPSA:
                return RDKFuncs.calcTPSA(rdkitMol);
        }
        LOG.log(Level.WARNING, "Function {0} not handled", function);
        return null;
    }
}
