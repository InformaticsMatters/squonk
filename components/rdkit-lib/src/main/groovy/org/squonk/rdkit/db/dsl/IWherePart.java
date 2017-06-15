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

package org.squonk.rdkit.db.dsl;

import org.squonk.rdkit.db.FingerprintType;
import org.squonk.rdkit.db.Metric;
import org.squonk.rdkit.db.MolSourceType;

import java.util.List;

/**
 * Created by timbo on 14/12/2015.
 */
public interface IWherePart {

    WhereClausePart similarityStructureQuery(String mol, MolSourceType molType, FingerprintType type, Metric metric, String outputColName);

    WhereClausePart substructureQuery(String mol, MolSourceType molType);

    WhereClausePart exactStructureQuery(String mol, MolSourceType molType);

    WhereClausePart equals(Column col, Object value);

    Select select();

    void appendToWhereClause(StringBuilder buf, List bindVars);
}
