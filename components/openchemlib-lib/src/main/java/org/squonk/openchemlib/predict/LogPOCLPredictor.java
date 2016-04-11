package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.CLogPPredictor;
import org.squonk.property.LogPProperty;
import org.squonk.property.MoleculeCalculator;

/**
 * Created by timbo on 05/04/16.
 */
public class LogPOCLPredictor extends AbstractOCLPredictor<Float> {

    private static final String NAME = "LogP_OCL";

    private CLogPPredictor predictor;

    public LogPOCLPredictor() {
        super(NAME, new LogPProperty());
    }


    private CLogPPredictor getPredictor() {
        if (predictor == null) {
            predictor = new CLogPPredictor();
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
            return getPredictor().assessCLogP(mol);
        }

    }
}
