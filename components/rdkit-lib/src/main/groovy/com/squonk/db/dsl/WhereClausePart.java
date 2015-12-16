package com.squonk.db.dsl;

import com.im.lac.types.MoleculeObject;
import com.squonk.db.rdkit.FingerprintType;
import com.squonk.db.rdkit.Metric;

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

    WhereClause getWhereClause() {
        return whereClause;
    }

    Select getSelect() {
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

    public WhereClausePart similarityStructureQuery(String smiles, FingerprintType type, Metric metric, String outputColName) {
        return similarityStructureQuery(whereClause, smiles, type, metric, outputColName);
    }

    public static WhereClausePart similarityStructureQuery(WhereClause whereClause, String smiles, FingerprintType type, Metric metric, String outputColName) {
        return new SimilarityStructureQuery(whereClause, smiles, type, metric, outputColName);
    }

    public WhereClausePart substructureQuery(String smarts) {
        return substructureQuery(whereClause, smarts);
    }

    public static WhereClausePart substructureQuery(WhereClause whereClause, String smarts) {
        return new SubstructureQuery(whereClause, smarts);
    }

    public WhereClausePart exactStructureQuery(String smiles) {
        return exactStructureQuery(whereClause, smiles);
    }

    public static WhereClausePart exactStructureQuery(WhereClause whereClause, String smiles) {
        return new ExactStructureQuery(whereClause, smiles);
    }

    public Select orderBy(Column col, boolean ascending) {
        return getSelect().orderBy(col, ascending);
    }

    public List<MoleculeObject> execute() {
        return getWhereClause().select.execute();
    }

    //abstract void appendToWhereClause(StringBuilder buf, List bindVars);

    public LimitClause limit(int limit) {
        return whereClause.select.limit(limit);
    }

    static class ExactStructureQuery extends WhereClausePart {

        private final String smiles;

        ExactStructureQuery(WhereClause where, String smiles) {
            super(where);
            this.smiles = smiles;
            where.select.join.enabled = true;
        }

        public void appendToWhereClause(StringBuilder buf, List bindVars) {
            buf.append("m.m = ?::mol");
            bindVars.add(smiles);
        }
    }

    static class SubstructureQuery extends WhereClausePart {

        final String smarts;

        SubstructureQuery(WhereClause where, String smarts) {
            super(where);
            this.smarts = smarts;
            where.select.join.enabled = true;
        }

        public void appendToWhereClause(StringBuilder buf, List bindVars) {
            buf.append("m.m @> ?::qmol");
            bindVars.add(smarts);
        }
    }

    static class SimilarityStructureQuery extends WhereClausePart {

        private final String smiles;
        private final FingerprintType type;
        private final Metric metric;
        private final String outputColName;

        SimilarityStructureQuery(WhereClause where, String smiles, FingerprintType type, Metric metric, String outputColName) {
            super(where);
            this.smiles = smiles;
            this.type = type;
            this.metric = metric;
            this.outputColName = outputColName;
            where.select.join.enabled = true;

            SimilarityFunction func = new SimilarityFunction();
            where.select.projections.add(func);
            where.select.orderByClause.add(func);
        }

        class SimilarityFunction implements IProjectionPart, IOrderByPart {
            @Override
            public int appendToProjections(StringBuilder builder, List bindVars) {
                String fpFunc = String.format(type.function + ',' + type.colName, "mol_from_smiles(?::cstring)");
                //                            morganbv_fp(%s,2)

                builder.append(String.format(metric.function + " AS %s", fpFunc, outputColName));
                //                           dice_sml(%s)
                //                      ->   dice_sml(morganbv_fp(mol_from_smiles(?::cstring),2),mfp2)

                bindVars.add(smiles);
                return 1;
            }

            public String getProjectionName() {
                return outputColName;
            }

            public int appendToOrderBy(StringBuilder buf) {
                buf.append(outputColName).append(" DESC");
                return 1;
            }
        }

        public void appendToWhereClause(StringBuilder buf, List bindVars) {

            buf.append(String.format(type.function, "mol_from_smiles(?::cstring)"))
                    .append(metric.operator)
                    .append("m.")
                    .append(type.colName);

            //"morganbv_fp(mol_from_smiles('CN1C=NC2=C1C(=O)N(C)C(=O)N2C'::cstring),2)#m.mfp2"

            bindVars.add(smiles);
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
            buf.append(col.table.name)
                    .append(".")
                    .append(col.name)
                    .append("=?");
            bindVars.add(value);
        }
    }
}
