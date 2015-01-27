import chemaxon.jchem.db.CacheRegistrationUtil
import chemaxon.jchem.db.JChemSearch
import chemaxon.util.ConnectionHandler
import chemaxon.sss.search.JChemSearchOptions
import chemaxon.util.ConnectionHandler
import org.postgresql.ds.PGSimpleDataSource

PGSimpleDataSource ds = new PGSimpleDataSource()
ds.serverName = 'localhost'
ds.portNumber =  49153
ds.databaseName = 'chemcentral'
ds.user = 'chemcentral'
ds.password = 'chemcentral'

def con = ds.connection
            
ConnectionHandler conh = new ConnectionHandler(con, 'chemcentral.jchemproperties')
//ConnectionHandler conh = new ConnectionHandler(con, 'vendordbs.jchemproperties')
CacheRegistrationUtil cru = new CacheRegistrationUtil(conh)
cru.registerCache()
println "cache registered"
            
try {
    println "setting autocommit to false"
    con.autoCommit = false
                    
    JChemSearch searcher = new JChemSearch()
    searcher.connectionHandler = conh
    searcher.structureTable = 'chemcentral.structures'
    //searcher.structureTable = 'vendordbs.emolecules_ordersc'
    searcher.queryStructure = 'CN1C=NC2=C1C(=O)N(C(=O)N2C)C'
 
    JChemSearchOptions searchOptions = new JChemSearchOptions(JChemSearch.FULL)
    searcher.setSearchOptions(searchOptions)
    searcher.setRunMode(JChemSearch.RUN_MODE_SYNCH_COMPLETE)
    println "starting search"
    long t0 = System.currentTimeMillis()
    searcher.run()
    long t1 = System.currentTimeMillis()
    println "search complete"
    println "autocommit is " + con.autoCommit
    con.commit()
    println "finished in ${t1-t0}ms"

} finally {
    cru.unRegisterCache()
    println "cache unregistered"
}