package rdkit

import org.squonk.db.rdkit.RDKitTable
import org.squonk.db.rdkit.dsl.IConfiguration
import org.squonk.db.rdkit.EMoleculesTable
import org.squonk.db.rdkit.MolSourceType

/**
 * Created by timbo on 16/12/2015.
 */
class EMoleculesLoader extends AbstractRDKitLoader{

    EMoleculesLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        ConfigObject props = LoaderUtils.createConfig(new File('loaders/rdkit_loader.properties').toURL())



        loadSdf(props)
    }


    static void loadSdf(ConfigObject props) {
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


    static void loadSmiles(ConfigObject props) {
        String baseTable = props.emolecules.table
        String schema = props.database.schema
        String file = props.emolecules.path + '/' + props.emolecules.file
        Map<String, Class> propertyToTypeMappings = props.emolecules.fields

        EMoleculesTable table = new EMoleculesTable(schema, baseTable, MolSourceType.SMILES)

        IConfiguration config = createConfiguration(props)
        int limit = 10000

        EMoleculesLoader loader = new EMoleculesLoader(table, config)
        loader.loadSmiles(file, limit, propertyToTypeMappings)
    }



}
