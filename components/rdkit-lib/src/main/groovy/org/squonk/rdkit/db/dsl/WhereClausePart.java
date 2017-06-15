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

import org.squonk.types.MoleculeObject;
import org.squonk.rdkit.db.FingerprintType;
import org.squonk.rdkit.db.Metric;
import org.squonk.rdkit.db.MolSourceType;

import java.util.List;

/**
 * Created by timbo on 13/12/2015.
 */
public abstract class WhereClausePart implements IExecutable, IWherePart {

    final WhereClause whereClause;

    WhereClausePart(WhereClause where) {
        this.whereClause = where;
        where.parts.add(this);
    }

    public WhereClause getWhereClause() {
        return whereClause;
    }

    public Select getSelect() {
        return whereClause.select;
    }

    public Select select() {
        return whereClause.select;
    }

    SqlQuery getQuery() {
        return getSelect().query;
    }

    public WhereClausePart equals(Column col, Object value) {
        return equals(whereClause, col, value);
    }

    public static WhereClausePart equals(WhereClause whereClause, Column col, Object value) {
        return new EqualsQuery(whereClause, col, value);
    }

    public WhereClausePart similarityStructureQuery(String mol, MolSourceType molType, FingerprintType fpType, Metric metric, String outputColName) {
        return similarityStructureQuery(whereClause, mol, molType, fpType, metric, outputColName);
    }

    public static WhereClausePart similarityStructureQuery(WhereClause whereClause, String mol, MolSourceType molType, FingerprintType fpType, Metric metric, String outputColName) {
        return new SimilarityStructureQuery(whereClause, mol, molType, fpType, metric, outputColName);
    }

    public WhereClausePart substructureQuery(String mol, MolSourceType molType) {
        return substructureQuery(whereClause, mol, molType);
    }

    public static WhereClausePart substructureQuery(WhereClause whereClause, String mol, MolSourceType molType) {
        return new SubstructureQuery(whereClause, mol, molType);
    }

    public WhereClausePart exactStructureQuery(String mol, MolSourceType molType) {
        return exactStructureQuery(whereClause, mol, molType);
    }

    public static WhereClausePart exactStructureQuery(WhereClause whereClause, String mol, MolSourceType molType) {
        return new ExactStructureQuery(whereClause, mol, molType);
    }

    public Select orderBy(Column col, boolean ascending) {
        return getSelect().orderBy(col, ascending);
    }

    public List<MoleculeObject> execute() {
        return getWhereClause().select.execute();
    }

    public LimitClause limit(int limit) {
        return whereClause.select.limit(limit);
    }

    static class ExactStructureQuery extends WhereClausePart {

        private final String mol;
        private final MolSourceType molType;

        ExactStructureQuery(WhereClause where, String mol, MolSourceType molType) {
            super(where);
            this.mol = mol;
            this.molType = molType;
            where.select.join.enabled = true;
        }

        public void appendToWhereClause(StringBuilder buf, List bindVars) {
            String structBindVar = "?" + (bindVars.size() +1);
            buf.append(whereClause.select.query.rdkTable.getMolFpTable().aliasOrSchemaPlusTable())
                    .append(".m = " + String.format(molType.molFunction, structBindVar+"::cstring"));
            bindVars.add(mol);
        }
    }

    static class SubstructureQuery extends WhereClausePart {

        private final String mol;
        private final MolSourceType molType;

        SubstructureQuery(WhereClause where, String mol, MolSourceType molType) {
            super(where);
            this.mol = mol;
            this.molType = molType;
            where.select.join.enabled = true;
        }

        public void appendToWhereClause(StringBuilder buf, List bindVars) {
            String structBindVar = "?" + (bindVars.size() +1);
            buf.append(whereClause.select.query.rdkTable.getMolFpTable().aliasOrSchemaPlusTable())
                    .append(".m @> " + String.format(molType.qmolFunction, structBindVar+"::cstring"));
            bindVars.add(mol);
        }
    }

    static class SimilarityStructureQuery extends WhereClausePart {

        private final String mol;
        private final MolSourceType molType;
        private final FingerprintType fpType;
        private final Metric metric;
        private final String outputColName;
        private String structBindVar;

        SimilarityStructureQuery(WhereClause where, String mol, MolSourceType molType, FingerprintType fpType, Metric metric, String outputColName) {
            super(where);
            this.mol = mol;
            this.molType = molType;
            this.fpType = fpType;
            this.metric = metric;
            this.outputColName = outputColName;
            where.select.join.enabled = true;

            SimilarityFunction func = new SimilarityFunction();
            where.select.projections.add(func);
            where.select.orderByClause.add(func);
        }

        protected String getStructParam(List bindVars) {
            if (structBindVar == null) {
                structBindVar = "?" + (bindVars.size() +1);
                bindVars.add(mol);
            }
            return structBindVar;
        }

        class SimilarityFunction implements IProjectionPart, IOrderByPart {

            @Override
            public int appendToProjections(StringBuilder builder, List bindVars) {
                String structParam = getStructParam(bindVars);
                String fpFunc = String.format(fpType.function + ',' + fpType.colName, String.format(molType.molFunction, structParam+"::cstring"));
                //                            morganbv_fp(%s,2)

                builder.append(String.format(metric.function + " AS %s", fpFunc, outputColName));
                //                           dice_sml(%s)
                //                      ->   dice_sml(morganbv_fp(mol_from_smiles(?::cstring),2),mfp2)
                //                      ->   dice_sml(morganbv_fp(mol_from_smiles(?::cstring),2),mfp2)
                return 1;
            }

            public String getProjectionName() {
                return outputColName;
            }

            public int appendToOrderBy(StringBuilder buf) {

                buf.append(String.format(fpType.function, String.format(molType.molFunction, structBindVar+"::cstring")))
                        .append("<").append(metric.operator).append(">")
                        .append(whereClause.select.query.rdkTable.getMolFpTable().aliasOrSchemaPlusTable())
                        .append(".")
                        .append(fpType.colName);
                return 1;

                //morganbv_fp(mol_from_smiles(?::cstring))<%>m.mfp2;
            }
        }

        public void appendToWhereClause(StringBuilder buf, List bindVars) {
            String structParam = getStructParam(bindVars);
            buf.append(String.format(fpType.function, String.format(molType.molFunction, structParam+"::cstring")))
                    .append(metric.operator)
                    .append(whereClause.select.query.rdkTable.getMolFpTable().aliasOrSchemaPlusTable())
                    .append(".")
                    .append(fpType.colName);

            //morganbv_fp(mol_from_smiles(?::cstring),2)%m.mfp2
        }
    }

    static class EqualsQuery extends WhereClausePart {

        final Column col;
        final Object value;

        EqualsQuery(WhereClause where, Column col, Object value) {
            super(where);
            this.col = col;
            this.value = value;
        }

        public void appendToWhereClause(StringBuilder buf, List bindVars) {
            String bindVar = "?" + (bindVars.size() +1);
            buf.append(col.table.name)
                    .append(".")
                    .append(col.name)
                    .append("=")
                    .append(bindVar);
            bindVars.add(value);
        }
    }
}
