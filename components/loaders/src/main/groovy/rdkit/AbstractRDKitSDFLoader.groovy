package rdkit

import com.im.lac.types.MoleculeObject
import com.squonk.db.rdkit.RDKitTable
import com.squonk.db.rdkit.RDKitTableLoader
import com.squonk.db.rdkit.RDKitTableSearch
import com.squonk.reader.SDFReader
import com.squonk.util.IOUtils
import javax.sql.DataSource
import java.util.stream.Stream


/**
 *
 * @author timbo
 */
class AbstractRDKitSDFLoader {
    
    protected DataSource dataSource
    protected String schema
    protected String baseTable
    protected RDKitTable.MolSourceType molSourceType
    protected Map<String,String> extraColumnDefs
    
     
    AbstractRDKitSDFLoader(DataSource dataSource, String schema, String baseTable, RDKitTable.MolSourceType molSourceType, Map<String,String> extraColumnDefs) {
        this.dataSource = dataSource
        this.schema = schema
        this.baseTable = baseTable
        this.molSourceType = molSourceType
        this.extraColumnDefs = extraColumnDefs
    }
    
    void load(String file, Map<String,Class> propertyToTypeMappings, List<RDKitTable.FingerprintType> fptypes, int limit) {
        println "Loading file $file"
        long t0 = System.currentTimeMillis()
        InputStream is = IOUtils.getGunzippedInputStream(new FileInputStream(file))
        try {
            SDFReader sdf = new SDFReader(is)
            Stream<MoleculeObject> mols = sdf.asStream()
            if (limit > 0) {
                mols = mols.limit(limit)
            }
        
            RDKitTableLoader worker = new RDKitTableLoader(dataSource, schema, baseTable, molSourceType, extraColumnDefs, propertyToTypeMappings)
            worker.dropAllItems()
            worker.createTables()
            worker.loadData(mols)
            worker.createMoleculesAndIndex()

            fptypes.each {  worker.addFpColumn(it) }
            
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
    
    void testSearcher() {
        long t0 = System.currentTimeMillis()
        RDKitTableSearch searcher = new RDKitTableSearch(dataSource, schema, baseTable, molSourceType, extraColumnDefs)
        def mols = searcher.exactSearch('CC(=O)OC1=CC=CC=C1C(O)=O', false)
        println "Found ${mols.size()} non-chiral exact search results"
        mols.each {
            println it
        }
        mols = searcher.substructureSearch('NC(=O)[C@@H]1CCCN1C=O', false, 10)
        println "Found ${mols.size()} non-chiral substructure search results"
        mols = searcher.substructureSearch('NC(=O)[C@@H]1CCCN1C=O', true)
        println "Found ${mols.size()} chiral substructure search results"
        mols = searcher.similaritySearch('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1', 0.5d, RDKitTable.FingerprintType.RDKIT, RDKitTable.Metric.TANIMOTO)
        println "Found ${mols.size()} similar results"
        mols = searcher.similaritySearch('Cc1ccc2nc(-c3ccc(NC(C4N(C(c5cccs5)=O)CCC4)=O)cc3)sc2c1', 0.4d, RDKitTable.FingerprintType.RDKIT, RDKitTable.Metric.TANIMOTO, 10)
        println "Found ${mols.size()} similar results"
//        mols.each {
//            println it
//        }
        long t1 = System.currentTimeMillis()
        println "Completed in ${t1-t0}ms"
    }
	
}

