package org.squonk.rdkit.db.dsl

import com.im.lac.types.MoleculeObject
import org.squonk.rdkit.db.MolSourceType
import groovy.sql.Sql
import groovy.util.logging.Log

/**
 * Created by timbo on 16/12/2015.
 */
@Log
class Executor {

    final Select select;

    Executor(Select select) {
        this.select = select;
    }

    String buildSql(List bindVars) {
        bindVars.clear();
        StringBuilder buf = new StringBuilder("SELECT ");

        // projections
        int count = 0;
        if (select.projections.size() == 0) {
            buf.append(select.query.rdkTable.name)
                    .append(".")
                    .append("*");
            count++;
        } else {
            for (IProjectionPart col : select.projections) {
                if (count > 0) {
                    buf.append(",");
                }
                col.appendToProjections(buf, bindVars);
                count++;
            }
        }

        // FROM clause
        buf.append("\n  FROM ").append(select.query.rdkTable.schemaPlusTableWithAlias());

        // JOIN clause
        select.join.append(buf);

        // WHERE clause
        select.whereClause.appendToWhereClause(buf, bindVars);

        // ORDER BY clause
        select.orderByClause.appendToOrderBy(buf);

        // LIMIT clause
        select.limitClause.append(buf);

        String sql = buf.toString();
        log.fine("SQL: " + sql);
        for (Object o : bindVars) {
            log.fine("  -> " + o.toString());
        }
        return sql;
    }

    List<MoleculeObject> execute() {

        List stmts = select.preExecuteStatements
        println("stmts: " + stmts)

        stmts.each {
            println("SQL:" + it.command)
        }

        Sql db = new Sql(select.query.config.connection)
        try {
            // 1 execute the preExecuteStatements
            select.preExecuteStatements.each {
                println("SQL:" + it.command)
                db.execute(it.command)
            }
            // 2 build the SQL
            List bindVars = []
            String sql = buildSql(bindVars)
            // 3 execute and build results
            List<MoleculeObject> mols = []
            String format = select.query.rdkTable.molSourceType == MolSourceType.MOL ? 'mol' : 'smiles'
            println("SQL:" + sql)
            db.eachRow(sql, bindVars) {
                mols << buildMoleculeObject(it.toRowResult(), format)
            }
            return mols
        } finally {
            db.close()
        }
    }

    private static final String STRUCTURE_COL = "structure";

    private MoleculeObject buildMoleculeObject(Map values, String format) {
        String structure = values.remove(STRUCTURE_COL)
        MoleculeObject mo = new MoleculeObject(structure, format)
        mo.putValues(values)
        return mo
    }

}
