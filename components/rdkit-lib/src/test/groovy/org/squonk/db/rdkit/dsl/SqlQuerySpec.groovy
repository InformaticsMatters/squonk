package org.squonk.db.rdkit.dsl

import org.squonk.db.rdkit.MolSourceType
import org.squonk.db.rdkit.FingerprintType
import org.squonk.db.rdkit.Metric
import org.squonk.db.rdkit.RDKitTable
import spock.lang.Specification

/**
 * Created by timbo on 13/12/2015.
 */
class SqlQuerySpec extends Specification {

    Table table = new RDKitTable("foo", MolSourceType.CTAB, [])
            .column("id", "SERIAL", "SERIAL PRIMARY KEY")
            .column("structure", "TEXT", "TEXT")
            .column("version_id", "INTEGER", "INTEGER NOT NULL")
            .column("parent_id", "INTEGER", "INTEGER NOT NULL")
    def query = new SqlQuery(table, null)
    def cols = query.columns;

    void "query with limit"() {
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

    void "query with 1 structure part"() {
        println "query with 1 structure part()"

        def q = query.select()
                .where().exactStructureQuery("c1ccccc1").whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 4
        bindVars.size() == 1
    }

    void "query with 1 similarity part"() {
        println "query with 1 similarity part()"

        def q = query.select()
                .where().similarityStructureQuery("c1ccccc1", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 5
        bindVars.size() == 2
        q.preExecuteStatements.size() == 0
    }

    void "query with 1 similarity part with threshold"() {
        println "query with 1 similarity part()"

        def q = query.select()
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("c1ccccc1", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 5
        bindVars.size() == 2
        q.preExecuteStatements.size() == 1
    }


    void "query with 1 equals part"() {
        println "query with 1 equals part()"

        def q = query.select()
                .where().equals(cols[0], 999).whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 3
        bindVars.size() == 1
    }

    void "query with 1 equals part + order by"() {
        println "query with 1 equals part + order by()"

        def q = query.select()
                .where().equals(cols[0], 999)
                .orderBy(cols[0], true)
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 4
        bindVars.size() == 1
    }

    void "query with 1 equals part + 2 order by"() {
        println "query with 1 equals part + 2 order by()"

        def q = query.select()
                .where().equals(cols[0], 999)
                .orderBy(cols[0], true)
                .orderBy(cols[2], false)
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 4
        bindVars.size() == 1
    }

    void "query with 2 mixed where parts"() {
        println "query with 2 whereClause parts()"

        def q = query.select()
                .where().equals(table.columns[0], 999).substructureQuery("CCCC")
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 5
        bindVars.size() == 2
    }

    void "query with 2 simple where parts"() {
        println "query with 2 whereClause parts()"

        def q = query.select()
                .where().equals(table.columns[0], 999).equals(table.columns[2], 8888)
                .select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 4
        bindVars.size() == 2
    }

    void "query with 2 where parts and limit"() {
        println "query with 2 whereClause parts and limit()"

        def q = query.select()
                .where().exactStructureQuery("c1ccccc1").substructureQuery("CCCC").limit(100)
                .select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 6
        bindVars.size() == 2
    }

    void "query with 1 equals part + projections"() {
        println "query with 1 equals part + projections()"

        def q = query.select(cols[0], cols[1])
                .where().equals(cols[0], 999)
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT t.id,t.structure')
        sql.readLines().size() == 3
        bindVars.size() == 1
    }

    void "query with 1 similarity part with threshold + projections"() {
        println "query with 1 similarity part with threshold + projections()"

        def q = query.select(cols[0], cols[1])
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("c1ccccc1", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT t.id,t.structure,')
        sql.readLines().size() == 5
        bindVars.size() == 2
        q.preExecuteStatements.size() == 1
    }

    void "query with 1 similarity part with threshold + projections + order by"() {
        println "query with 1 similarity part with threshold + projections()"

        def q = query.select(cols[0], cols[1])
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("c1ccccc1", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .orderBy(cols[2], true)
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT t.id,t.structure,')
        sql.readLines().size() == 5
        bindVars.size() == 2
        q.preExecuteStatements.size() == 1
    }

    void "query + order by with 1 similarity part with threshold + projections"() {
        println "query with 1 similarity part with threshold + projections()"

        def q = query.select(cols[0], cols[1])
                .setSimilarityThreshold(0.7, Metric.DICE)
                .orderBy(cols[2], true)
                .where().similarityStructureQuery("c1ccccc1", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        def sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT t.id,t.structure,')
        sql.readLines().size() == 5
        bindVars.size() == 2
        q.preExecuteStatements.size() == 1
    }
}
