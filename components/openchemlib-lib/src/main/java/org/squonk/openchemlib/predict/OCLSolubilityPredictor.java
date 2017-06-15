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

package org.squonk.openchemlib.predict;

import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.prediction.SolubilityPredictor;
import org.squonk.property.AqueousSolubilityProperty;
import org.squonk.property.MoleculeCalculator;
import org.squonk.util.Metrics;

import static org.squonk.util.Metrics.PROVIDER_OPENCHEMLIB;

/**
 * Created by timbo on 05/04/16.
 */
public class OCLSolubilityPredictor extends AbstractOCLPredictor<Float> {

    private static final String NAME = "AqSol_OCL";
    private static final String CODE = Metrics.generate(PROVIDER_OPENCHEMLIB, AqueousSolubilityProperty.METRICS_CODE);

    private SolubilityPredictor predictor;

    public OCLSolubilityPredictor() {
        super(NAME, new AqueousSolubilityProperty());
    }


    private SolubilityPredictor getPredictor() {
        if (predictor == null) {
            predictor = new SolubilityPredictor();
        }
        return predictor;
    }

    @Override
    public MoleculeCalculator<Float>[] getCalculators() {
        return new MoleculeCalculator[] {new Calc(NAME)};
    }

    class Calc extends AbstractOCLPredictor.OCLCalculator {

        Calc(String resultName) {
            super(resultName, Float.class);
        }


        protected Float doCalculate(StereoMolecule mol) {
            float result = getPredictor().assessSolubility(mol);
            incrementExecutionCount(CODE, 1);
            return result;
        }

    }
}
