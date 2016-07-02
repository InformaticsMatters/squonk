package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.PolarSurfaceAreaPredictor;
import org.squonk.property.MoleculeCalculator;
import org.squonk.property.PSAProperty;
import org.squonk.util.Metrics;
import static org.squonk.util.Metrics.*;

/**
 * Created by timbo on 05/04/16.
 */
public class OCLPSAPredictor extends AbstractOCLPredictor<Float> {

    private static final String NAME = "PSA_OCL";
    private static final String CODE = Metrics.generate(PROVIDER_OPENCHEMLIB, PSAProperty.METRICS_CODE);


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
            incrementExecutionCount(CODE, 1);
            return result;
        }

    }
}
