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
public enum FingerprintType {
    RDKIT("rdkit_fp(%s)", "rdk"),
    MORGAN_CONNECTIVITY_2("morganbv_fp(%s,2)", "mfp2"),
    MORGAN_CONNECTIVITY_3("morganbv_fp(%s,3)", "mfp3"),
    MORGAN_FEATURE_2("featmorganbv_fp(%s,2)", "ffp2"),
    MORGAN_FEATURE_3("featmorganbv_fp(%s,3)", "ffp3"),
    TORSION("torsionbv_fp(%s)", "tfp"),
    MACCS("maccs_fp(%s)", "maccsfp");

    public String function;
    public String colName;

    FingerprintType(String function, String col) {
        this.function = function;
        this.colName = col;
    }
}
