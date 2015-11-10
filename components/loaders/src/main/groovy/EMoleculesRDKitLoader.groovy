
import com.im.lac.types.MoleculeObject
import com.squonk.reader.SDFReader
import com.squonk.util.IOUtils
import com.squonk.rdkit.db.*
import groovy.sql.Sql
import java.sql.SQLException
import java.util.stream.Stream
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
class EMoleculesRDKitLoader {
    
    static void main(String[] args) {
        
        ConfigObject database = Utils.createConfig('rdkit_cartridge.properties')
        ConfigObject props = Utils.createConfig(new File('emolecules_rdkit.properties').toURL())
        String baseTable = props.table
        String schema = database.externaldbs.schema
        Map<String,Class> propertyToTypeMappings = props.fields
        DataSource dataSource = Utils.createDataSource(database, database.externaldbs.username, database.externaldbs.password)
        Map<String,String> extraColumnDefs = props.extraColumnDefs
        String file = props.path + '/' + props.file
        
        load(file, dataSource, schema, baseTable, RDKitTable.MolSourceType.CTAB, extraColumnDefs, propertyToTypeMappings)
        
        testSearcher(dataSource, schema, baseTable, RDKitTable.MolSourceType.CTAB, extraColumnDefs)
    }
    
    static void load(String file, DataSource dataSource, String schema, String baseTable, RDKitTable.MolSourceType molSourceType, Map<String,String> extraColumnDefs, Map<String,Class> propertyToTypeMappings) {
        println "Loading file $file"
        long t0 = System.currentTimeMillis()
        InputStream is = IOUtils.getGunzippedInputStream(new FileInputStream(file))
        try {
            SDFReader sdf = new SDFReader(is)
            Stream<MoleculeObject> mols = sdf.asStream() //.limit(1000)
        
            RDKitTableLoader worker = new RDKitTableLoader(dataSource, schema, baseTable, molSourceType, extraColumnDefs, propertyToTypeMappings)
            worker.dropAllItems()
            worker.createTables()
            worker.loadData(mols)
            worker.createMoleculesAndIndex()

            worker.addFpColumn(RDKitTable.FingerprintType.RDKIT)
            worker.addFpColumn(RDKitTable.FingerprintType.MORGAN_CONNECTIVITY_2)
            worker.addFpColumn(RDKitTable.FingerprintType.MORGAN_FEATURE_2)
            //            worker.addFpColumn(RDKitTable.FingerprintType.MORGAN_CONNECTIVITY_3)
            //            worker.addFpColumn(RDKitTable.FingerprintType.MORGAN_FEATURE_3)
            //            worker.addFpColumn(RDKitTable.FingerprintType.MACCS)
            //            worker.addFpColumn(RDKitTable.FingerprintType.TORSION)
            
            worker.getRowCount()
            int hits = worker.testSSS()
            println "$hits SSS hits"
            hits = worker.testFpSearch(RDKitTable.FingerprintType.RDKIT, RDKitTable.Metric.TANIMOTO)
            println "$hits RDKit hits using tanimoto"
            hits = worker.testFpSearch(RDKitTable.FingerprintType.RDKIT, RDKitTable.Metric.DICE)
            println "$hits RDKit hits using dice"
            hits = worker.testFpSearch(RDKitTable.FingerprintType.MORGAN_CONNECTIVITY_2, RDKitTable.Metric.TANIMOTO)
            println "$hits MFP2 hits using tanimoto"
            hits = worker.testFpSearch(RDKitTable.FingerprintType.MORGAN_CONNECTIVITY_2, RDKitTable.Metric.DICE)
            println "$hits MFP2 hits using dice"
            hits = worker.testFpSearch(RDKitTable.FingerprintType.MORGAN_FEATURE_2, RDKitTable.Metric.TANIMOTO)
            println "$hits FFP2 hits using tanimoto"
            hits = worker.testFpSearch(RDKitTable.FingerprintType.MORGAN_FEATURE_2, RDKitTable.Metric.DICE)
            println "$hits FFP2 hits using dice"
            
            //worker.dropAllItems()
           
        } finally {
            is.close()
        }
        long t1 = System.currentTimeMillis()
        println "Completed in ${t1-t0}ms"
    }
    
    static void testSearcher(DataSource dataSource, String schema, String baseTable, RDKitTable.MolSourceType molSourceType, Map<String,String> extraColumnDefs) {
        long t0 = System.currentTimeMillis()
        RDKitTableSearch searcher = new RDKitTableSearch(dataSource, schema, baseTable, molSourceType, extraColumnDefs)
        def mols = searcher.substructureSearch('NC(=O)[C@@H]1CCCN1C=O', false, 10)
        println "Found ${mols.size()} non-chiral results"
        mols = searcher.substructureSearch('NC(=O)[C@@H]1CCCN1C=O', true)
        println "Found ${mols.size()} chiral results"
        mols = searcher.similaritySearch('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1', 0.5d, RDKitTable.FingerprintType.RDKIT, RDKitTable.Metric.TANIMOTO)
        println "Found ${mols.size()} similar results"
        mols = searcher.similaritySearch('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1', 0.4d, RDKitTable.FingerprintType.RDKIT, RDKitTable.Metric.TANIMOTO, 10)
        println "Found ${mols.size()} similar results"
        mols.each {
            println it
        }
        long t1 = System.currentTimeMillis()
        println "Completed in ${t1-t0}ms"
    }
   
}

