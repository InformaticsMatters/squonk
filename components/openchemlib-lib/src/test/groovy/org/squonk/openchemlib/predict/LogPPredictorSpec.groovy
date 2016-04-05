package org.squonk.openchemlib.predict

import com.im.lac.types.MoleculeObject
import org.squonk.data.Molecules
import spock.lang.Specification

/**
 * Created by timbo on 05/04/16.
 */
class LogPPredictorSpec extends Specification {

    void "test with smiles"() {

        LogPPredictor predictor = new LogPPredictor()

        when:
        def result = predictor.calculate(new MoleculeObject(Molecules.ethanol.smiles, "smiles"))

        then:
        result != null
    }

    void "test with molfile"() {

        LogPPredictor predictor = new LogPPredictor()

        when:
        def result = predictor.calculate(new MoleculeObject(Molecules.ethanol.v2000, "mol"))
        println "logp: $result"
        then:
        result != null
    }
}
