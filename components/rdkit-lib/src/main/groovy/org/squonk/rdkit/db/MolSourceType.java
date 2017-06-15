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
public enum MolSourceType {

    MOL("mol_from_ctab(%s)", "qmol_from_ctab(%s)"), SMILES("mol_from_smiles(%s)", "qmol_from_smiles(%s)"), SMARTS("mol_from_smarts(%s)", "mol_from_smarts(%s)");

    public String molFunction;
    public String qmolFunction;

    MolSourceType(String molFunction, String qmolFunction) {
        this.molFunction = molFunction;
        this.qmolFunction = qmolFunction;
    }
}
