
import grails.orm.bootstrap.*
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.h2.Driver
import com.foo.domain.*


init = new HibernateDatastoreSpringInitializer('com.foo.domain')
def dataSource = new DriverManagerDataSource(Driver.name, "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE", 'sa', '')
init.configureForDataSource(dataSource)

println "Total funny people = " + Person.count()

//BaseMolecule mol1 = new BaseMolecule(1, 'C').save()
//BaseMolecule mol2 = new BaseMolecule(2, 'CC').save()
//println "Total base mols = " + BaseMolecule.count()

MyMolecule mm1 = new MyMolecule(1, 'C', 'methane').save()
MyMolecule mm2 = new MyMolecule(2, 'CC', 'ethane').save()

println "Total mymols = " + MyMolecule.count()