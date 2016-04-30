package org.squonk.rdkit.db.loaders

import org.squonk.rdkit.db.impl.DrugbankTable
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration

/**
 * Created by timbo on 16/12/2015.
 */
class DrugBankSDFLoader extends AbstractRDKitLoader {

    DrugBankSDFLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = loadConfigFile()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.drugbank.table
        String schema = props.database.schema
        String file = props.drugbank.path + '/' + props.drugbank.file
        int reportingChunk = props.drugbank.reportingChunk
        int loadOnly = props.drugbank.loadOnly
        Map<String, Class> propertyToTypeMappings = props.drugbank.fields

        println "Loading $file into ${schema}.$baseTable"

        DrugbankTable table = new DrugbankTable(schema, baseTable)

        IConfiguration config = createConfiguration(props)

        DrugBankSDFLoader loader = new DrugBankSDFLoader(table, config)
        loader.loadSDF(file, loadOnly, reportingChunk, propertyToTypeMappings)
    }

}
