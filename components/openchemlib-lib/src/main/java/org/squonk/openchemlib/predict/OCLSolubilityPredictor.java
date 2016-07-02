package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.SolubilityPredictor;
import org.squonk.property.AqueousSolubilityProperty;
import org.squonk.property.MoleculeCalculator;
import org.squonk.util.Metrics;

import static org.squonk.util.Metrics.PROVIDER_OPENCHEMLIB;

/**
 * Created by timbo on 05/04/16.
 */
public class OCLSolubilityPredictor extends AbstractOCLPredictor<Float> {

    private static final String NAME = "AqSol_OCL";
    private static final String CODE = Metrics.generate(PROVIDER_OPENCHEMLIB, AqueousSolubilityProperty.METRICS_CODE);

    private SolubilityPredictor predictor;

    public OCLSolubilityPredictor() {
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
            float result = getPredictor().assessSolubility(mol);
            incrementExecutionCount(CODE, 1);
            return result;
        }

    }
}
