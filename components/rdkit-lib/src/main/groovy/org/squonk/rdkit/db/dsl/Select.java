package org.squonk.rdkit.db.dsl;

import com.im.lac.types.MoleculeObject;
import org.squonk.rdkit.db.Metric;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by timbo on 13/12/2015.
 */
public class Select {
    private static final Logger LOG = Logger.getLogger(Select.class.getName());

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
        this.join = new FpTableJoin(this, query.rdkTable.getMolFpTable().schemaPlusTable());
        this.whereClause = new WhereClause(this);
        this.orderByClause = new OrderByClause(this);
        this.limitClause = new LimitClause(this, 0);
    }

    public void setconfiguration(IConfiguration configuration) {
        query.setConfiguration(configuration);
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
        return setOption("SET rdkit.do_chiral_sss=" + (chiral ? "true" : "false"));
    }

    public Select setSimilarityThreshold(double threshold, Metric metric) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("Threshold must be betwen 0 and 1. Value specified was " + threshold);
        }
        return setOption("SET " + metric.simThresholdProp + "=" + threshold);
    }

    Select setOption(String stmt) {
        LOG.info("Adding pre-execute statement: " + stmt);
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
