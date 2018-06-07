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

import groovy.sql.Sql
import org.postgresql.ds.PGSimpleDataSource
import org.squonk.rdkit.db.ChemcentralConfig
import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.Metric
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.loaders.AbstractRDKitLoader
import org.squonk.util.IOUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 30/04/16.
 */
@Stepwise
class SqlQueryDbSpec extends Specification {

    static String baseTable = "emols_test"
    static String schema = "vendordbs"
    static RDKitTable table = new EMoleculesTable(schema, baseTable)
    static RDKitTable alias = table.alias("rdk")
    static String qSmiles = 'OC1CC2(C(C1CC2=O)(C)C)C'
    static String qSmarts = '[#6]C1([#6])[#6]-2-[#6]-[#6](=O)C1([#6])[#6]-[#6]-2-[#8]'
    static String qMol = '''
  Mrv0541 05011615002D

 12 13  0  0  0  0            999 V2000
   -0.0249    1.4008    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
    0.2733    0.5639    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.2543   -0.2609    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.8472   -0.6569    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.6599    0.1726    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    0.8850    0.9927    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    1.5899    0.5639    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    1.5709   -0.2609    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    2.3460   -0.6496    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0
   -0.3099   -0.2049    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
   -0.2996    0.5066    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
    1.0407   -1.5090    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
  1  2  1  0  0  0  0
  2  3  1  0  0  0  0
  3  4  1  0  0  0  0
  4  5  1  0  0  0  0
  5  6  1  0  0  0  0
  2  6  1  0  0  0  0
  6  7  1  0  0  0  0
  7  8  1  0  0  0  0
  4  8  1  0  0  0  0
  8  9  2  0  0  0  0
  5 10  1  0  0  0  0
  5 11  1  0  0  0  0
  4 12  1  0  0  0  0
M  END
'''

    @Shared PGSimpleDataSource dataSource

    void setupSpec() {
        dataSource = new PGSimpleDataSource()
        dataSource.serverName = IOUtils.getConfiguration('PRIVATE_HOST', 'localhost')
        dataSource.portNumber = 5432
        dataSource.databaseName = 'chemcentral'
        dataSource.user = 'chemcentral'
        dataSource.password = 'chemcentral'
    }

    void "load emols"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        String filename = "../../data/testfiles/emols_100.smi.gz"
        AbstractRDKitLoader loader = new AbstractRDKitLoader(table) {

            @Override
            void load() {
                loadSmiles(filename, 100, 10, ['1':Integer.class, '2':Integer.class])
            }
        }
        Sql db = new Sql(dataSource)

        when:
        loader.load()
        int rows = db.firstRow("select count(*) from " + schema + "." + baseTable)[0]
        println "$rows loaded"

        then:
        rows == 100

        cleanup:
        db?.close()
    }

    void "query with limit"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)

        def q = query.select()
                .limit(10)
                .select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 10
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "query with limit using alias"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(alias, config)

        def q = query.select()
                .limit(10)
                .select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 10
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "query with limit with projections"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)

        def q = query.select(table.columns[1], table.columns[2]) // 1 is structure, 2 is version_id
                .limit(10)
                .select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 10
        mols[0].values["id"] == null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "exact with 1 structure part smiles"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().exactStructureQuery(qSmiles, MolSourceType.SMILES).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "exact with 1 structure part mol"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().exactStructureQuery(qMol, MolSourceType.MOL).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "exact with 1 structure part using alias"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(alias, config)
        def q = query.select()
                .where().exactStructureQuery(qSmiles, MolSourceType.SMILES).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "exact with 1 structure part with projections using alias"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(alias, config)
        def q = query.select(alias.columns[1], alias.columns[2]) // 1 is structure, 2 is version_id
                .where().exactStructureQuery(qSmiles, MolSourceType.SMILES).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] == null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "sss with 1 structure part smiles"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().substructureQuery(qSmiles, MolSourceType.SMILES).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "sss with 1 structure part smarts"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().substructureQuery(qSmarts, MolSourceType.SMARTS).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "sss with 1 structure part mol"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().substructureQuery(qMol, MolSourceType.MOL).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "similarity with 1 part smiles"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().similarityStructureQuery(qSmiles, MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
        mols[0].values["similarity"] != null
    }

    void "similarity with 1 part mol"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().similarityStructureQuery(qMol, MolSourceType.MOL, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
        mols[0].values["similarity"] != null
    }

    void "similarity with 1 part with projections"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select(table.columns[1], table.columns[2]) // 1 is structure, 2 is version_id
                .where().similarityStructureQuery(qSmiles, MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] == null
        mols[0].values["version_id"] != null
        mols[0].source != null
        mols[0].values["similarity"] != null
    }

    void "similarity with 1 part using alias"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(alias, config)
        def q = query.select()
                .where().similarityStructureQuery(qSmiles, MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
        mols[0].values["similarity"] != null
    }

    void "similarity with 1 part with projections using alias"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(alias, config)
        def q = query.select(alias.columns[1], alias.columns[2]) // 1 is structure, 2 is version_id
                .where().similarityStructureQuery(qSmiles, MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] == null
        mols[0].values["version_id"] != null
        mols[0].source != null
        mols[0].values["similarity"] != null
    }

    void "similarity with 1 part with threshold"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery(qSmiles, MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
        mols[0].values["similarity"] != null
    }

    void "similarity with 1 part with threshold using alias"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(alias, config)
        def q = query.select()
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery(qSmiles, MolSourceType.SMILES, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
                .whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
        mols[0].values["similarity"] != null
    }

    void "query with 1 equals part"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().equals(table.columns[0], 5).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "query with 1 equals part using alias"() {

        ChemcentralConfig config = new ChemcentralConfig(dataSource, null)
        def query = new SqlQuery(alias, config)
        def q = query.select()
                .where().equals(alias.columns[0], 5).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }


    static class EMoleculesTable extends RDKitTable {

        EMoleculesTable(String schema, String baseTableName) {
            super(schema, baseTableName, MolSourceType.SMILES, [
                    FingerprintType.RDKIT,
                    FingerprintType.MORGAN_CONNECTIVITY_2,
                    FingerprintType.MORGAN_FEATURE_2])
            addColumn("version_id", "INTEGER", "INTEGER NOT NULL")
            addColumn("parent_id", "INTEGER", "INTEGER NOT NULL")
        }
    }

}
