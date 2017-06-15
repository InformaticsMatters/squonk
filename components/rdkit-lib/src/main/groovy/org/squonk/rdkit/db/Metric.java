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

package org.squonk.rdkit.db;

/**
 * Created by timbo on 13/12/2015.
 */
public enum Metric {
    TANIMOTO("%", "tanimoto_sml(%s)", "rdkit.tanimoto_threshold"),
    DICE("#", "dice_sml(%s)", "rdkit.dice_threshold");

    public String operator;
    public String function;
    public String simThresholdProp;

    Metric(String operator, String function, String simThresholdProp) {
        this.operator = operator;
        this.function = function;
        this.simThresholdProp = simThresholdProp;
    }
}
