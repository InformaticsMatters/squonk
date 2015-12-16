package com.squonk.db.dsl

import com.squonk.db.rdkit.FingerprintType
import com.squonk.db.rdkit.Metric
import com.squonk.db.rdkit.MolSourceType
import org.postgresql.ds.PGSimpleDataSource
import spock.lang.Specification

import javax.sql.DataSource

/**
 * Created by timbo on 13/12/2015.
 */
class SqlQueryExecuteSpec extends Specification {

    Table table = new RdkTable("emolecules_order_bb", MolSourceType.CTAB, [])
            .column("id", "SERIAL", "SERIAL PRIMARY KEY")
            .column("structure", "TEXT", "TEXT")
            .column("version_id", "INTEGER", "INTEGER NOT NULL")
            .column("parent_id", "INTEGER", "INTEGER NOT NULL")

    def query = new SqlQuery(table, new DataSourceConfiguration(createDataSource(), [:]))
    def cols = query.columns;

    DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource()
        ds.serverName = "192.168.99.100"
        ds.portNumber = 5432
        ds.databaseName = "rdkit"
        ds.user = "docker"
        ds.password = "docker"
        return ds
    }

    void "query with limit"() {
        println "query with limit()"

        def q = query.select()
                .limit(10)
                .select
        def e = q.executor

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)
        def results = e.execute()

        then:
        sql != null
        sql.readLines().size() == 3
        bindVars.size() == 0
        results.size() == 10
    }


}
