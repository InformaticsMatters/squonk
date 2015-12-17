package rdkit

import com.squonk.db.rdkit.DrugbankTable
import com.squonk.db.rdkit.EMoleculesTable
import com.squonk.db.rdkit.MolSourceType
import com.squonk.db.rdkit.RDKitTable
import com.squonk.db.rdkit.dsl.IConfiguration

/**
 * Created by timbo on 16/12/2015.
 */
class DrugBankLoader extends AbstractRDKitLoader {

    DrugBankLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        ConfigObject props = LoaderUtils.createConfig(new File('loaders/rdkit_loader.properties').toURL())
        String baseTable = props.drugbank.table
        String schema = props.database.schema
        String file = props.drugbank.path + '/' + props.drugbank.file
        Map<String, Class> propertyToTypeMappings = props.drugbank.fields

        DrugbankTable table = new DrugbankTable(schema, baseTable)

        IConfiguration config = createConfiguration(props)
        int limit = -1

        DrugBankLoader loader = new DrugBankLoader(table, config)
        loader.loadSDF(file, limit, propertyToTypeMappings)
    }


}
