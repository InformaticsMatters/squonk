package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.im.lac.types.MoleculeObject;
import org.squonk.property.AqueousSolubilityProperty;

/**
 * Created by timbo on 05/04/16.
 */
public class SolubilityPredictor extends AbstractPredictor<Float, MoleculeObject> {

    private com.actelion.research.chem.prediction.SolubilityPredictor predictor;

    public SolubilityPredictor() {
        super("AqSol_OCL", new AqueousSolubilityProperty());
    }


    private com.actelion.research.chem.prediction.SolubilityPredictor getPredictor() {
        if (predictor == null) {
            predictor = new com.actelion.research.chem.prediction.SolubilityPredictor();
        }
        return predictor;
    }

    @Override
    public Float calculate(StereoMolecule mol) {
        return getPredictor().assessSolubility(mol);
    }
}
