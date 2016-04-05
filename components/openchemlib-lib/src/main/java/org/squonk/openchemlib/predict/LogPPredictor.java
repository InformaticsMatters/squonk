package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.CLogPPredictor;
import com.im.lac.types.MoleculeObject;
import org.squonk.openchemlib.molecule.OCLMoleculeUtils;
import org.squonk.property.LogPProperty;

/**
 * Created by timbo on 05/04/16.
 */
public class LogPPredictor extends AbstractPredictor<Float,MoleculeObject> {

    private CLogPPredictor predictor;

    public LogPPredictor() {
        super("LogP_OCL", new LogPProperty());
    }


    private CLogPPredictor getPredictor() {
        if (predictor == null) {
            predictor = new CLogPPredictor();
        }
        return predictor;
    }

    @Override
    public Float calculate(StereoMolecule mol) {
        return getPredictor().assessCLogP(mol);
    }
}
