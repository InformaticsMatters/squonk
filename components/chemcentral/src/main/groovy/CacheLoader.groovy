import javax.sql.DataSource
import java.sql.Connection
import groovy.sql.Sql
import com.im.lac.dwsearch.util.Utils

ConfigObject database = new ConfigSlurper().parse(getClass().getClassLoader().getResource('database.properties'))
ConfigObject chemcentral = new ConfigSlurper().parse(getClass().getClassLoader().getResource('chemcentral.properties'))

DataSource dataSource = Utils.createDataSource(database, chemcentral.username, chemcentral.password)
Connection con = dataSource.connection
con.setAutoCommit(true)
Sql db = new Sql(con)
int count = 0
println "starting ..."
long t0 = System.currentTimeMillis()
db.withStatement{ stmt -> stmt.setFetchSize(10000) }
db.eachRow('''SELECT cd_id, cd_smiles, cd_formula, cd_sortable_formula, cd_molweight, cd_hash,
cd_flags, cd_timestamp, cd_pre_calculated, cd_taut_hash, cd_taut_frag_hash, cd_screen_descriptor,
cd_fp1, cd_fp2, cd_fp3, cd_fp4, cd_fp5, cd_fp6, cd_fp7, cd_fp8, cd_fp9, cd_fp10,
cd_fp11, cd_fp12, cd_fp13, cd_fp14, cd_fp15, cd_fp16
FROM chemcentral.structures''') {
    count++
    if ((count % 100000) == 0) {
        println count
    }
}
long t1 = System.currentTimeMillis()
println "Finished in ${t1-t0}ms"
