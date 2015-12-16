package com.squonk.db.dsl;

import com.im.lac.types.MoleculeObject;
import com.squonk.db.rdkit.Metric;

import java.util.*;

/**
 * Created by timbo on 13/12/2015.
 */
public class Select {
    final SqlQuery query;
    final List<IProjectionPart> projections = new ArrayList<>();

    final List<SimpleStatement> preExecuteStatements = new ArrayList<>();
    final FpTableJoin join;
    final WhereClause whereClause;
    final OrderByClause orderByClause;
    LimitClause limitClause;

    public Select(SqlQuery query, Column... cols) {
        this.query = query;
        this.projections.addAll(Arrays.asList(cols));
        this.join = new FpTableJoin(this, query.rdkTable.molfpsTable.baseName);
        this.whereClause = new WhereClause(this);
        this.orderByClause = new OrderByClause(this);
        this.limitClause = new LimitClause(this, 0);
    }

    public WhereClause where() {
        return whereClause;
    }

    public LimitClause limit(int limit) {
        this.limitClause = new LimitClause(this, limit);
        return this.limitClause;
    }

    public Select orderBy(Column col, boolean ascending) {
        this.orderByClause.add(col, ascending);
        return this;
    }

    WhereClause getWhereClause() {
        return this.whereClause;
    }

    LimitClause getLimitClause() {
        return this.limitClause;
    }


    public Select setChiral(boolean chiral) {
        return setOption("SET rdkit.do_chiral_sss=" + chiral);
    }

    public Select setSimilarityThreshold(double threshold, Metric metric) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be betwen 0 and 1. Value specified was " + threshold);
        }
        return setOption("SET " + metric.simThresholdProp + "=" + threshold);
    }

    Select setOption(String stmt) {
        preExecuteStatements.add(new SimpleStatement(stmt));
        return this;
    }

    List<SimpleStatement> getPreExecuteStatements() {
        return preExecuteStatements;
    }

    public Executor getExecutor() {
        return new Executor(this);
    }

    public List<MoleculeObject> execute() {
        return getExecutor().execute();
    }
}
