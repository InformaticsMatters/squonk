package rdkit

import com.im.lac.types.MoleculeObject
import org.squonk.db.rdkit.dsl.DataSourceConfiguration
import org.squonk.db.rdkit.dsl.IConfiguration
import org.squonk.db.rdkit.RDKitTable
import org.squonk.db.rdkit.dsl.SqlQuery
import org.squonk.db.rdkit.RDKitTableLoader
import org.squonk.reader.SDFReader
import org.squonk.util.IOUtils

import javax.sql.DataSource
import java.util.stream.Stream

/**
 * Created by timbo on 16/12/2015.
 */
class AbstractRDKitLoader {

    protected final RDKitTable table
    protected IConfiguration config

    AbstractRDKitLoader(RDKitTable table, IConfiguration config) {
        this.table = table
        this.config = config
    }


    static IConfiguration createConfiguration(ConfigObject props) {
        DataSource dataSource = LoaderUtils.createDataSource(props.database, props.database.username, props.database.password)
        return new DataSourceConfiguration(dataSource, [:])
    }

    void loadSmiles(String file, int limit, Map<String, Class> propertyToTypeMappings) {

    }

    void loadSDF(String file, int limit, Map<String, Class> propertyToTypeMappings) {
        SqlQuery q = new SqlQuery(table, config)

        println "Loading file $file"
        long t0 = System.currentTimeMillis()
        InputStream is = IOUtils.getGunzippedInputStream(new FileInputStream(file))
        try {
            SDFReader sdf = new SDFReader(is)
            Stream<MoleculeObject> mols = sdf.asStream()
            if (limit > 0) {
                mols = mols.limit(limit)
            }

            doLoad(q.loader(), mols, propertyToTypeMappings)

            //worker.dropAllItems()

        } finally {
            is.close()
        }
        long t1 = System.currentTimeMillis()
        println "Completed in ${t1 - t0}ms"
    }

    private void doLoad(RDKitTableLoader worker, Stream<MoleculeObject> mols, Map<String, Class> propertyToTypeMappings) {

        worker.dropAllItems()
        worker.createTables()
        worker.loadData(mols, propertyToTypeMappings)
        worker.createMoleculesAndIndex()
        worker.addFpColumns()

        worker.getRowCount()
    }
}
