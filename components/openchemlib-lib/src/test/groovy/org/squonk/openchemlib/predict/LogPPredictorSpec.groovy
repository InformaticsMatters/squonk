package org.squonk.openchemlib.predict

import com.im.lac.types.MoleculeObject
import org.squonk.data.Molecules
import org.squonk.property.Calculator
import spock.lang.Specification

/**
 * Created by timbo on 05/04/16.
 */
class LogPPredictorSpec extends Specification {

    void "test with smiles"() {

        LogPOCLPredictor predictor = new LogPOCLPredictor()

        when:
        def result = predictor.calculator.calculate(new MoleculeObject(Molecules.ethanol.smiles, "smiles"))

        then:
        result != null
    }

    void "test with molfile"() {

        LogPOCLPredictor predictor = new LogPOCLPredictor()
        Calculator calc = predictor.calculator

        when:
        def result = calc.calculate(new MoleculeObject(Molecules.ethanol.v2000, "mol"))
        println "logp: $result"

        then:
        result != null
        calc.totalCount == 1
        calc.errorCount == 0
    }
}
