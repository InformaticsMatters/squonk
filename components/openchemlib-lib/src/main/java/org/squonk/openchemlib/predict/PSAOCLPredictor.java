package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.PolarSurfaceAreaPredictor;
import com.im.lac.types.MoleculeObject;
import org.squonk.property.Calculator;
import org.squonk.property.MoleculeCalculator;
import org.squonk.property.PSAProperty;

/**
 * Created by timbo on 05/04/16.
 */
public class PSAOCLPredictor extends AbstractOCLPredictor<Float> {

    private PolarSurfaceAreaPredictor predictor;

    public PSAOCLPredictor() {
        super("PSA_OCL", new PSAProperty());
    }


    private PolarSurfaceAreaPredictor getPredictor() {
        if (predictor == null) {
            predictor = new PolarSurfaceAreaPredictor();
        }
        return predictor;
    }

    @Override
    public MoleculeCalculator<Float> getCalculator() {
        return new Calc();
    }

    class Calc extends AbstractOCLPredictor.OCLCalculator {

        protected Float doCalculate(StereoMolecule mol) {
            return getPredictor().assessPSA(mol);
        }

    }
}
