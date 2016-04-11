package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.CLogPPredictor;
import com.im.lac.types.MoleculeObject;
import org.squonk.property.Calculator;
import org.squonk.property.LogPProperty;
import org.squonk.property.MoleculeCalculator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 05/04/16.
 */
public class LogPOCLPredictor extends AbstractOCLPredictor<Float> {

    private static final Logger LOG = Logger.getLogger(LogPOCLPredictor.class.getName());

    private CLogPPredictor predictor;

    public LogPOCLPredictor() {
        super("LogP_OCL", new LogPProperty());
    }


    private CLogPPredictor getPredictor() {
        if (predictor == null) {
            predictor = new CLogPPredictor();
        }
        return predictor;
    }

    @Override
    public MoleculeCalculator<Float> getCalculator() {
        return new Calc();
    }

    class Calc extends AbstractOCLPredictor.OCLCalculator {

        protected Float doCalculate(StereoMolecule mol) {
            return getPredictor().assessCLogP(mol);
        }

    }
}
