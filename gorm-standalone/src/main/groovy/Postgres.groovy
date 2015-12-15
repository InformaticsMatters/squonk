
import grails.orm.bootstrap.*
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.h2.Driver
import com.foo.domain.*


PGSimpleDataSource ds = new PGSimpleDataSource()
ds.serverName = '192.168.99.100'
ds.portNumber = 5432
ds.databaseName = 'rdkit'
ds.user = 'docker'
ds.password = 'docker'

init = new HibernateDatastoreSpringInitializer('com.foo.domain')
init.configureForDataSource(ds)

println "Total funny people = " + Person.count()

MyMolecule mm1 = new MyMolecule('C', 'methane').save()
MyMolecule mm2 = new MyMolecule('CC', 'ethane').save()

println "Total mymols = " + MyMolecule.count()