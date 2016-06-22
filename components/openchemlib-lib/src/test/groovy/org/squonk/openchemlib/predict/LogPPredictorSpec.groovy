package org.squonk.openchemlib.predict

import org.squonk.types.MoleculeObject
import org.squonk.data.Molecules
import org.squonk.property.Calculator
import spock.lang.Specification

/**
 * Created by timbo on 05/04/16.
 */
class LogPPredictorSpec extends Specification {

    void "test with smiles"() {

        OCLLogPPredictor predictor = new OCLLogPPredictor()

        when:
        def result = predictor.calculators[0].calculate(new MoleculeObject(Molecules.ethanol.smiles, "smiles"))

        then:
        result != null
    }

    void "test with molfile"() {

        OCLLogPPredictor predictor = new OCLLogPPredictor()
        Calculator calc = predictor.calculators[0]

        when:
        def result = calc.calculate(new MoleculeObject(Molecules.ethanol.v2000, "mol"))
        println "logp: $result"

        then:
        result != null
        calc.totalCount == 1
        calc.errorCount == 0
    }
}
