package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.im.lac.types.MoleculeObject;
import org.squonk.property.AqueousSolubilityProperty;
import org.squonk.property.Calculator;
import org.squonk.property.MoleculeCalculator;

/**
 * Created by timbo on 05/04/16.
 */
public class SolubilityOCLPredictor extends AbstractOCLPredictor<Float> {

    private com.actelion.research.chem.prediction.SolubilityPredictor predictor;

    public SolubilityOCLPredictor() {
        super("AqSol_OCL", new AqueousSolubilityProperty());
    }


    private com.actelion.research.chem.prediction.SolubilityPredictor getPredictor() {
        if (predictor == null) {
            predictor = new com.actelion.research.chem.prediction.SolubilityPredictor();
        }
        return predictor;
    }

    @Override
    public MoleculeCalculator<Float> getCalculator() {
        return new Calc();
    }

    class Calc extends AbstractOCLPredictor.OCLCalculator {

        protected Float doCalculate(StereoMolecule mol) {
            return getPredictor().assessSolubility(mol);
        }

    }
}
