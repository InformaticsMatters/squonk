package org.squonk.db

import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.SqlQuery
import org.squonk.rdkit.db.dsl.Table
import spock.lang.Specification

/**
 * Created by timbo on 16/12/2015.
 */
class SqlQueryX extends Specification {

    Table table = new RDKitTable("foo", MolSourceType.CTAB, [])
            .column("id", "SERIAL", "SERIAL PRIMARY KEY")
            .column("structure", "TEXT", "TEXT")
            .column("version_id", "INTEGER", "INTEGER NOT NULL")
            .column("parent_id", "INTEGER", "INTEGER NOT NULL")
    def query = new SqlQuery(table, null)
    def cols = query.columns;

    void "query with limit different package"() {
        println "query with limit()"

        def q = query.select()
                .limit(100)
                .select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 3
        bindVars.size() == 0
    }

}
