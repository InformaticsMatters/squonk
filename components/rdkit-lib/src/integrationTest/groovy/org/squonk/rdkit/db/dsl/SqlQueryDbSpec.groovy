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

    void "exact with 1 structure part smiles"() {

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().exactStructureQuery(qMol, MolSourceType.CTAB).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() == 1
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "exact with 1 structure part using alias"() {

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().substructureQuery(qMol, MolSourceType.CTAB).whereClause.select

        when:
        def mols = q.executor.execute()

        then:
        mols.size() > 0
        mols[0].values["id"] != null
        mols[0].values["version_id"] != null
        mols[0].source != null
    }

    void "similarity with 1 part smiles"() {

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
        def query = new SqlQuery(table, config)
        def q = query.select()
                .where().similarityStructureQuery(qMol, MolSourceType.CTAB, FingerprintType.MORGAN_CONNECTIVITY_2, Metric.DICE, "similarity")
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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

        IConfiguration config = new DataSourceConfiguration(dataSource, [:])
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
