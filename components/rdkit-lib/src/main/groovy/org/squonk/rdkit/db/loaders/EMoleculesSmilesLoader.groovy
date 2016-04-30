package org.squonk.rdkit.db.loaders

import org.squonk.rdkit.db.impl.EMoleculesTable
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable
import org.squonk.rdkit.db.dsl.IConfiguration

/**
 * Created by timbo on 16/12/2015.
 */
class EMoleculesSmilesLoader extends AbstractRDKitLoader {

    EMoleculesSmilesLoader(RDKitTable table, IConfiguration config) {
        super(table, config)
    }


    static void main(String[] args) {

        URL from = loadConfigFile()
        println "Loading from $from"
        ConfigObject props = LoaderUtils.createConfig(from)
        String baseTable = props.emolecules.table
        String schema = props.database.schema
        String file = props.emolecules.path + '/' + props.emolecules.file
        int reportingChunk = props.emolecules.reportingChunk
        int loadOnly = props.emolecules.loadOnly
        Map<String, Class> propertyToTypeMappings = props.emolecules.fields

        println "Loading $file into ${schema}.$baseTable"

        EMoleculesTable table = new EMoleculesTable(schema, baseTable, MolSourceType.SMILES)

        IConfiguration config = createConfiguration(props)

        EMoleculesSmilesLoader loader = new EMoleculesSmilesLoader(table, config)
        loader.loadSmiles(file, loadOnly, reportingChunk, propertyToTypeMappings)
    }

}
