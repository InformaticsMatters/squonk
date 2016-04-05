package org.squonk.openchemlib.predict

import com.im.lac.types.MoleculeObject
import org.squonk.data.Molecules
import spock.lang.Specification

/**
 * Created by timbo on 05/04/16.
 */
class PSAPredictorSpec extends Specification {

    void "test with smiles"() {

        PSAPredictor predictor = new PSAPredictor()

        when:
        def result = predictor.calculate(new MoleculeObject(Molecules.ethanol.smiles, "smiles"))

        then:
        result != null
    }

    void "test with molfile"() {

        PSAPredictor predictor = new PSAPredictor()

        when:
        def result = predictor.calculate(new MoleculeObject(Molecules.ethanol.v2000, "mol"))

        then:
        result != null
    }
}
