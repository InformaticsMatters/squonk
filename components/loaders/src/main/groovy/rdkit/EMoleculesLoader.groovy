package rdkit

import com.squonk.db.rdkit.RDKitTable
import com.squonk.db.rdkit.dsl.IConfiguration
import com.squonk.db.rdkit.EMoleculesTable
import com.squonk.db.rdkit.MolSourceType
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader

/**
 * Created by timbo on 16/12/2015.
 */
class EMoleculesLoader extends AbstractRDKitLoader{

    EMoleculesLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        ConfigObject props = LoaderUtils.createConfig(new File('loaders/rdkit_loader.properties').toURL())
        String baseTable = props.emolecules.table
        String schema = props.database.schema
        String file = props.emolecules.path + '/' + props.emolecules.file
        Map<String, Class> propertyToTypeMappings = props.emolecules.fields

        EMoleculesTable table = new EMoleculesTable(schema, baseTable, MolSourceType.CTAB)

        IConfiguration config = createConfiguration(props)
        int limit = 10000

        EMoleculesLoader loader = new EMoleculesLoader(table, config)
        loader.loadSDF(file, limit, propertyToTypeMappings)
    }



}
