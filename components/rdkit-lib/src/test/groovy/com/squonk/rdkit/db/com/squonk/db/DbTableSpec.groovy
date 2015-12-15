package com.squonk.rdkit.db.com.squonk.db

import com.squonk.db.DbTable
import spock.lang.Specification

/**
 * Created by timbo on 08/12/2015.
 */
class DbTableSpec extends Specification {

    void "create table ddl"() {

        DbTable t = new DbTable("schema", "table")
                .withColumn("id", "SERIAL")
                .withColumn("structure", "TEXT")
        t.setPrimaryKey(t.columns[0])

        when:
        String ddl = t.buildCreateTableSql()
        println ddl

        then:
        ddl != null
    }
}
