package rdkit

import com.im.lac.types.MoleculeObject
import com.squonk.db.dsl.DataSourceConfiguration
import com.squonk.db.dsl.IConfiguration
import com.squonk.db.dsl.RdkTable
import com.squonk.db.dsl.SqlQuery
import com.squonk.db.rdkit.EMoleculesTable
import com.squonk.db.rdkit.FingerprintType
import com.squonk.db.rdkit.MolSourceType
import com.squonk.db.rdkit.RDKitTableLoader
import com.squonk.reader.SDFReader
import com.squonk.util.IOUtils

import javax.sql.DataSource
import java.util.stream.Stream

/**
 * Created by timbo on 16/12/2015.
 */
class EMoleculesLoader extends AbstractRDKitLoader{


    static void main(String[] args) {

        ConfigObject props = Utils.createConfig(new File('loaders/rdkit_loader.properties').toURL())
        String baseTable = props.emolecules.table
        String schema = props.database.schema
        String file = props.emolecules.path + '/' + props.emolecules.file
        Map<String, Class> propertyToTypeMappings = props.emolecules.fields

        EMoleculesTable emolsTable = new EMoleculesTable(schema, baseTable, MolSourceType.CTAB)

        IConfiguration config = createConfiguration(props)
        int limit = 10000

        EMoleculesLoader loader = new EMoleculesLoader()
        loader.loadSDF(file, limit, propertyToTypeMappings, emolsTable, config)
    }



}
