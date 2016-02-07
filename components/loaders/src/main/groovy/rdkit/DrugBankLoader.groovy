package rdkit

import org.squonk.db.rdkit.DrugbankTable
import org.squonk.db.rdkit.RDKitTable
import org.squonk.db.rdkit.dsl.IConfiguration

/**
 * Created by timbo on 16/12/2015.
 */
class DrugBankLoader extends AbstractRDKitLoader {

    DrugBankLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = new File('loaders/rdkit_loader.properties').toURI().toURL()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
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
