/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.openchemlib.predict

import org.squonk.types.MoleculeObject
import org.squonk.data.Molecules
import spock.lang.Specification

/**
 * Created by timbo on 05/04/16.
 */
class PSAPredictorSpec extends Specification {

    void "test with smiles"() {

        OCLPSAPredictor predictor = new OCLPSAPredictor()

        when:
        def result = predictor.calculators[0].calculate(new MoleculeObject(Molecules.ethanol.smiles, "smiles"))

        then:
        result != null
    }

    void "test with molfile"() {

        OCLPSAPredictor predictor = new OCLPSAPredictor()

        when:
        def result = predictor.calculators[0].calculate(new MoleculeObject(Molecules.ethanol.v2000, "mol"))

        then:
        result != null
    }
}
