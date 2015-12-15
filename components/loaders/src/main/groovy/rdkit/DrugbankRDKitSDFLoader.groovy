package rdkit


import com.squonk.rdkit.db.*
import groovy.transform.InheritConstructors
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
@InheritConstructors
class DrugbankRDKitSDFLoader extends AbstractRDKitSDFLoader {
    

    static void main(String[] args) {
        
        ConfigObject props = Utils.createConfig(new File('rdkit_loader.properties').toURL())
        String baseTable = props.drugbank.table
        String schema = props.database.schema
        Map<String,Class> propertyToTypeMappings = props.drugbank.fields
        DataSource dataSource = Utils.createDataSource(props.database, props.database.username, props.database.password)
        Map<String,String> extraColumnDefs = props.drugbank.extraColumnDefs
        String file = props.drugbank.path + '/' + props.drugbank.file
        
        DrugbankRDKitSDFLoader loader = new DrugbankRDKitSDFLoader(dataSource, schema, baseTable, com.squonk.db.rdkit.RDKitTable.MolSourceType.CTAB, extraColumnDefs)
        
        loader.load(file, propertyToTypeMappings, [
                com.squonk.db.rdkit.RDKitTable.FingerprintType.RDKIT,
                com.squonk.db.rdkit.RDKitTable.FingerprintType.MORGAN_CONNECTIVITY_2,
                com.squonk.db.rdkit.RDKitTable.FingerprintType.MORGAN_FEATURE_2],
        -1)
        
        loader.testSearcher()
    }
   
}

