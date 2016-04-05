package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.CLogPPredictor;
import com.actelion.research.chem.prediction.PolarSurfaceAreaPredictor;
import com.im.lac.types.MoleculeObject;
import org.squonk.openchemlib.molecule.OCLMoleculeUtils;
import org.squonk.property.LogPProperty;
import org.squonk.property.PSAProperty;

/**
 * Created by timbo on 05/04/16.
 */
public class PSAPredictor extends AbstractPredictor<Float,MoleculeObject> {

    private PolarSurfaceAreaPredictor predictor;

    public PSAPredictor() {
        super("PSA_OCL", new PSAProperty());
    }


    private PolarSurfaceAreaPredictor getPredictor() {
        if (predictor == null) {
            predictor = new PolarSurfaceAreaPredictor();
        }
        return predictor;
    }

    @Override
    public Float calculate(StereoMolecule mol) {
        return getPredictor().assessPSA(mol);
    }
}
