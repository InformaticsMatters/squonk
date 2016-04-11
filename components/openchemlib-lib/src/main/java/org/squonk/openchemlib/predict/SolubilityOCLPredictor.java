package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.SolubilityPredictor;
import com.im.lac.types.MoleculeObject;
import org.squonk.property.AqueousSolubilityProperty;
import org.squonk.property.Calculator;
import org.squonk.property.MoleculeCalculator;

/**
 * Created by timbo on 05/04/16.
 */
public class SolubilityOCLPredictor extends AbstractOCLPredictor<Float> {

    private static final String NAME = "AqSol_OCL";

    private SolubilityPredictor predictor;

    public SolubilityOCLPredictor() {
        super(NAME, new AqueousSolubilityProperty());
    }


    private SolubilityPredictor getPredictor() {
        if (predictor == null) {
            predictor = new SolubilityPredictor();
        }
        return predictor;
    }

    @Override
    public MoleculeCalculator<Float>[] getCalculators() {
        return new MoleculeCalculator[] {new Calc(NAME)};
    }

    class Calc extends AbstractOCLPredictor.OCLCalculator {

        Calc(String resultName) {
            super(resultName, Float.class);
        }


        protected Float doCalculate(StereoMolecule mol) {
            return getPredictor().assessSolubility(mol);
        }

    }
}
