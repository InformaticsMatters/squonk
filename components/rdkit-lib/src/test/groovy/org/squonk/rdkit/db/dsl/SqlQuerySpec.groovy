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

package org.squonk.rdkit.db.dsl

import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.Metric
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import spock.lang.Specification

/**
 * Created by timbo on 13/12/2015.
 */
class SqlQuerySpec extends Specification {

    Table table = new RDKitTable("foo", MolSourceType.MOL, [])
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
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 3
        bindVars.size() == 0
    }

    void "query with 1 structure part"() {
        println "query with 1 structure part()"

        def q = query.select()
                .where().exactStructureQuery("c1ccccc1", MolSourceType.SMILES).whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 4
        bindVars.size() == 1
    }

    void "query with 1 similarity part"() {
        println "query with 1 similarity part()"

        def q = query.select()
                .where().similarityStructureQuery("c1ccccc1", MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 5
        bindVars.size() == 1
        q.preExecuteStatements.size() == 0
    }

    void "query with 1 similarity part with threshold"() {
        println "query with 1 similarity part with threshold"

        def q = query.select()
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("c1ccccc1", MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 5
        bindVars.size() == 1
        q.preExecuteStatements.size() == 1
    }


    void "query with 1 equals part"() {
        println "query with 1 equals part()"

        def q = query.select()
                .where().equals(cols[0], 999).whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

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
        String sql = q.executor.buildSql(bindVars)

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
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 4
        bindVars.size() == 1
    }

    void "query with 2 mixed where parts"() {
        println "query with 2 whereClause parts()"

        def q = query.select()
                .where().equals(table.columns[0], 999).substructureQuery("CCCC", MolSourceType.SMILES)
                .whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

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
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.readLines().size() == 4
        bindVars.size() == 2
    }

    void "query with 2 where parts and limit"() {
        println "query with 2 whereClause parts and limit()"

        def q = query.select()
                .where().exactStructureQuery("c1ccccc1", MolSourceType.SMILES).substructureQuery("CCCC", MolSourceType.SMILES).limit(100)
                .select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

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
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT foo.id,foo.structure')
        sql.readLines().size() == 3
        bindVars.size() == 1
    }

    void "query with 1 similarity part with threshold + projections"() {
        println "query with 1 similarity part with threshold + projections()"

        def q = query.select(cols[0], cols[1])
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("c1ccccc1", MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT foo.id,foo.structure,')
        sql.readLines().size() == 5
        bindVars.size() == 1
        q.preExecuteStatements.size() == 1
    }

    void "query with 1 similarity part with threshold + projections + order by"() {
        println "query with 1 similarity part with threshold + projections()"

        def q = query.select(cols[0], cols[1])
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("c1ccccc1", MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .orderBy(cols[2], true)
                .whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT foo.id,foo.structure,')
        sql.readLines().size() == 5
        bindVars.size() == 1
        q.preExecuteStatements.size() == 1
    }

    void "query + order by with 1 similarity part with threshold + projections"() {
        println "query with 1 similarity part with threshold + projections()"

        def q = query.select(cols[0], cols[1])
                .setSimilarityThreshold(0.7, Metric.DICE)
                .orderBy(cols[2], true)
                .where().similarityStructureQuery("c1ccccc1", MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        List bindVars = []
        String sql = q.executor.buildSql(bindVars)

        then:
        sql != null
        sql.startsWith('SELECT foo.id,foo.structure,')
        sql.readLines().size() == 5
        bindVars.size() == 1
        q.preExecuteStatements.size() == 1
    }
}
