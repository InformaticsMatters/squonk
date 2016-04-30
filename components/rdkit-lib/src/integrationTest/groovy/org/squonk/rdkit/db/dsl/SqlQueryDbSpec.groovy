package org.squonk.rdkit.db.dsl

import groovy.sql.Sql
import org.postgresql.ds.PGSimpleDataSource
import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.Metric
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.impl.EMoleculesTable
import org.squonk.rdkit.db.loaders.EMoleculesSmilesLoader
import org.squonk.util.IOUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.sql.DataSource

/**
 * Created by timbo on 30/04/16.
 */
@Stepwise
class SqlQueryDbSpec extends Specification {

    static String baseTable = "emols_test"
    static String schema = "vendordbs"
    static RDKitTable table = new EMoleculesTable(schema, baseTable, MolSourceType.SMILES)
    static RDKitTable alias = table.alias("rdk")

    @Shared PGSimpleDataSource dataSource

    void setupSpec() {
        dataSource = new PGSimpleDataSource()
        dataSource.serverName = IOUtils.getConfiguration('PRIVATE_HOST', 'localhost')
        dataSource.portNumber = 5432
        dataSource.databaseName = 'chemcentral'
        dataSource.user = 'squonk'
        dataSource.password = 'squonk'
    }


    void "load emols"() {

        String file = "../../data/testfiles/emols_order_all_100.smi.gz"
        println "Loading $file into ${schema}.$baseTable"
        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        EMoleculesSmilesLoader loader = new EMoleculesSmilesLoader(table, config)
        Sql db = new Sql(dataSource)

        when:
        loader.loadSmiles(file, 0, 10, ['1':Integer.class, '2':Integer.class])
        int rows = db.firstRow("select count(*) from " + schema + "." + baseTable)[0]
        println "$rows loaded"

        then:
        rows == 100

        cleanup:
        db.close()
    }

    void "query with limit"() {
        println "query with limit()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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
        println "query with limit()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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
        println "query with limit()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

    void "query with 1 structure part"() {
        println "query with 1 structure part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().exactStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C").whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null

    }

    void "query with 1 structure part using alias"() {
        println "query with 1 structure part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(alias, config)
        def q = query.select()
                .where().exactStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C").whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "query with 1 structure part with projections using alias"() {
        println "query with 1 structure part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(alias, config)
        def q = query.select(alias.columns[1], alias.columns[2]) // 1 is structure, 2 is version_id
                .where().exactStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C").whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] == null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "query with 1 similarity part"() {
        println "query with 1 similarity part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().similarityStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
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

    void "query with 1 similarity part with projections"() {
        println "query with 1 similarity part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(table, config)
        def q = query.select(table.columns[1], table.columns[2]) // 1 is structure, 2 is version_id
                .where().similarityStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
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

    void "query with 1 similarity part using alias"() {
        println "query with 1 similarity part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(alias, config)
        def q = query.select()
                .where().similarityStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
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

    void "query with 1 similarity part with projections using alias"() {
        println "query with 1 similarity part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(alias, config)
        def q = query.select(alias.columns[1], alias.columns[2]) // 1 is structure, 2 is version_id
                .where().similarityStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
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

    void "query with 1 similarity part with threshold"() {
        println "query with 1 similarity part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(table, config)
        def q = query.select()
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
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

    void "query with 1 similarity part with threshold using alias"() {
        println "query with 1 similarity part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(alias, config)
        def q = query.select()
                .setSimilarityThreshold(0.7, Metric.DICE)
                .where().similarityStructureQuery("OC1CC2(C(C1CC2=O)(C)C)C", FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
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
        println "query with 1 equals part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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
        println "query with 1 equals part()"

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

}
