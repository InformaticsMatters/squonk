package org.squonk.db.rdkit.dsl

import com.im.lac.types.MoleculeObject
import org.squonk.db.rdkit.MolSourceType
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

    public String buildSql(List bindVars) {
        bindVars.clear();
        StringBuilder buf = new StringBuilder("SELECT ");

        // projections
        int count = 0;
        if (select.projections.size() == 0) {
            buf.append(select.query.aliasTable.name)
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

        // from
        buf.append("\n  FROM ")
                .append(select.query.rdkTable.schemaPlusTable())
                .append(" t");

        // join clause
        select.join.append(buf);

        // where clause
        select.whereClause.appendToWhereClause(buf, bindVars);

        // order by
        select.orderByClause.appendToOrderBy(buf);

        // limit clause
        select.limitClause.append(buf);

        String sql = buf.toString();
        System.out.println("SQL: " + sql);
        for (Object o : bindVars) {
            System.out.println("  -> " + o.toString());
        }
        return sql;
    }

    public List<MoleculeObject> execute() {

        Sql db = new Sql(select.query.config.connection)
        try {
            // 1 execute the preExecuteStatements
            select.preExecuteStatements.each {
                log.info("SQL:" + it.command)
                db.execute(it.command)
            }
            // 2 build the SQL
            List bindVars = []
            String sql = buildSql(bindVars)
            // 3 execute and build results
            List<MoleculeObject> mols = []
            String format = select.query.rdkTable.molSourceType == MolSourceType.CTAB ? 'mol' : 'smiles'
            log.info("SQL:" + sql)
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
