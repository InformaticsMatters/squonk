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
import org.squonk.types.MoleculeObject;
import org.squonk.openchemlib.molecule.OCLMoleculeUtils;
import org.squonk.property.MoleculeCalculator;
import org.squonk.property.Predictor;
import org.squonk.property.Property;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 05/04/16.
 */
public abstract class AbstractOCLPredictor<V> extends Predictor<V,MoleculeObject,MoleculeCalculator<V>> {

    private static final Logger LOG = Logger.getLogger(AbstractOCLPredictor.class.getName());

    public AbstractOCLPredictor(String resultName, Property<V,MoleculeObject> propertyType) {
        super(resultName, propertyType);
    }


    public abstract class OCLCalculator<V> extends MoleculeCalculator<V> {

        public OCLCalculator(String resultName, Class<V> resultType) {
            super(resultName, resultType);
        }

        @Override
        public V calculate(MoleculeObject mo, boolean storeResult, boolean storeMol) {
            StereoMolecule mol = OCLMoleculeUtils.fetchMolecule(mo, storeMol);
            V result = calculate(mol);
            if (storeResult) {
                mo.putValue(resultName, result);
            }
            return result;
        }

        @Override
        public V calculate(MoleculeObject mo, boolean storeResult) {
            return calculate(mo, storeResult, false);
        }

        @Override
        public V calculate(MoleculeObject mo) {
            return calculate(mo, false, false);
        }

        public V calculate(StereoMolecule mol) {
            totalCount.incrementAndGet();
            try {
                return doCalculate(mol);
            } catch (Throwable t) {
                errorCount.incrementAndGet();
                LOG.log(Level.INFO, "OCL calculation " + resultName + " failed", t);
                return null;
            }
        }

        protected abstract V doCalculate(StereoMolecule mol);
    }
}
