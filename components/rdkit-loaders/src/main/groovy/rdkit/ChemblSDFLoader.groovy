package rdkit

import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration
import org.squonk.rdkit.db.impl.ChemblTable
import org.squonk.rdkit.db.impl.DrugbankTable

/**
 * Created by timbo on 16/12/2015.
 */
class ChemblSDFLoader extends AbstractRDKitLoader {

    ChemblSDFLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = loadConfigFile()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.chembl.table
        String schema = props.database.schema
        String file = props.chembl.path + '/' + props.chembl.file
        int reportingChunk = props.chembl.reportingChunk
        int loadOnly = props.chembl.loadOnly
        Map<String, Class> propertyToTypeMappings = props.chembl.fields

        println "Loading $file into ${schema}.$baseTable"

        ChemblTable table = new ChemblTable(schema, baseTable)

        IConfiguration config = createConfiguration(props)

        ChemblSDFLoader loader = new ChemblSDFLoader(table, config)
        loader.loadSDF(file, loadOnly, reportingChunk, propertyToTypeMappings, null)
    }

}
