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

import spock.lang.Specification

/**
 * Created by timbo on 13/12/2015.
 */
class SqlQueryExecuteSpec extends Specification {

//    Table table = new RDKitTable("emolecules_order_bb", MolSourceType.MOL, [])
//            .column("id", "SERIAL", "SERIAL PRIMARY KEY")
//            .column("structure", "TEXT", "TEXT")
//            .column("version_id", "INTEGER", "INTEGER NOT NULL")
//            .column("parent_id", "INTEGER", "INTEGER NOT NULL")
//
//    def query = new SqlQuery(table, new DataSourceConfiguration(createDataSource(), [:]))
//    def cols = query.columns;
//
//    DataSource createDataSource() {
//        PGSimpleDataSource ds = new PGSimpleDataSource()
//        ds.serverName = "192.168.99.100"
//        ds.portNumber = 5432
//        ds.databaseName = "rdkit"
//        ds.user = "docker"
//        ds.password = "docker"
//        return ds
//    }
//
//    void "query with limit"() {
//        println "query with limit()"
//
//        def q = query.select()
//                .limit(10)
//                .select
//        def e = q.executor
//
//        when:
//        List bindVars = []
//        def sql = q.executor.buildSql(bindVars)
//        def results = e.execute()
//
//        then:
//        sql != null
//        sql.readLines().size() == 3
//        bindVars.size() == 0
//        results.size() == 10
//    }
//

}
