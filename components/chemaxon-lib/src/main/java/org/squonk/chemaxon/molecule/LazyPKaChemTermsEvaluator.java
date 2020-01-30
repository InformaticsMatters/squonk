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
package org.squonk.chemaxon.molecule;

import chemaxon.nfunk.jep.ParseException;
import org.squonk.util.Metrics;

import java.util.Map;
import java.util.stream.Collectors;

import static org.squonk.util.Metrics.METRICS_PKA;
import static org.squonk.util.Metrics.PROVIDER_CHEMAXON;

public class LazyPKaChemTermsEvaluator extends LazyChemTermsEvaluator {

    private final String pkaTypePropName;

    /**
     *
     * @param pkaTypePropName The property name (the key in the Map) from the config that is passed in to the #doInit()
     *                        method that contains the type of pKa calculation. The value for that key in the Map must
     *                        be "acidic" or "basic"
     * @param resultPropName The name of the property to use to store the calculation result. If null then default names
     *                       are used.
     * @throws ParseException
     */
    public LazyPKaChemTermsEvaluator(String pkaTypePropName, String resultPropName) throws ParseException {
        super(resultPropName, null, Mode.Calculate, Metrics.generate(PROVIDER_CHEMAXON, METRICS_PKA));
        this.pkaTypePropName = pkaTypePropName;
    }

    public LazyPKaChemTermsEvaluator(String pkaTypePropName) throws ParseException {
        this(pkaTypePropName, null);
    }


    @Override
    protected MoleculeEvaluator doInit(Map<String, Object> config) {
        String pkaType = (String)config.get(pkaTypePropName);
        if (pkaType == null) {
            String keys = config.keySet().stream().collect(Collectors.joining(","));
            throw new IllegalStateException("Property named " + pkaTypePropName +
                    " for the type of pKa calculation must be defined in the config. Keys that were found are: " + keys);
        }
        try {
            String propName = getPropName();
            if ("acidic".equals(pkaType)) {
                return new ChemTermsEvaluator(
                        propName == null ? ChemTermsEvaluator.APKA : propName,
                        "acidicpKa('1')", getMetricsCode()
                );
            } else if ("basic".equals(pkaType)) {
                return new ChemTermsEvaluator(
                        propName == null ? ChemTermsEvaluator.BPKA : propName,
                        "basicpKa('1')", getMetricsCode()
                );
            }
        } catch (ParseException pe) {
            throw new RuntimeException("Failed to create ChemTermsEvaluator for pKa");
        }

        return null;
    }
}
