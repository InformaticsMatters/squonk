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

package org.squonk.property;

import org.squonk.util.Metrics;

/**
 * Created by timbo on 05/04/16.
 */
public class AqueousSolubilityProperty extends MoleculeObjectProperty {

    public static final String METRICS_CODE = Metrics.METRICS_LOGS;
    public static final String PROP_NAME = "Solubility";
    public static final String PROP_DESC = "Aqueous Solubility";

    public AqueousSolubilityProperty() {
        super(PROP_NAME, PROP_DESC, METRICS_CODE, Float.class);
    }
}
