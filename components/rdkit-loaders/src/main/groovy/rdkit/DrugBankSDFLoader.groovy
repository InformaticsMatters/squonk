package rdkit

import org.squonk.db.rdkit.DrugbankTable
import org.squonk.db.rdkit.RDKitTable
import org.squonk.db.rdkit.dsl.IConfiguration

/**
 * Created by timbo on 16/12/2015.
 */
class DrugBankSDFLoader extends AbstractRDKitLoader {

    DrugBankSDFLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = new File('rdkit-loaders/rdkit_loader.properties').toURI().toURL()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.drugbank.table
        String schema = props.database.schema
        String file = props.drugbank.path + '/' + props.drugbank.file
        int reportingChunk = props.drugbank.reportingChunk
        int loadOnly = props.emolecules.loadOnly
        Map<String, Class> propertyToTypeMappings = props.drugbank.fields

        println "Loading $file into ${schema}.$baseTable"

        DrugbankTable table = new DrugbankTable(schema, baseTable)

        IConfiguration config = createConfiguration(props)

        DrugBankSDFLoader loader = new DrugBankSDFLoader(table, config)
        loader.loadSDF(file, loadOnly, reportingChunk, propertyToTypeMappings)
    }

}
