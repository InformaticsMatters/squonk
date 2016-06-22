package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.PolarSurfaceAreaPredictor;
import org.squonk.property.MoleculeCalculator;
import org.squonk.property.PSAProperty;

/**
 * Created by timbo on 05/04/16.
 */
public class OCLPSAPredictor extends AbstractOCLPredictor<Float> {

    private static final String NAME = "PSA_OCL";

    private PolarSurfaceAreaPredictor predictor;

    public OCLPSAPredictor() {
        super(NAME, new PSAProperty());
    }


    private PolarSurfaceAreaPredictor getPredictor() {
        if (predictor == null) {
            predictor = new PolarSurfaceAreaPredictor();
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
            float result = getPredictor().assessPSA(mol);
            incrementExecutionCount(NAME, 1);
            return result;
        }

    }
}
