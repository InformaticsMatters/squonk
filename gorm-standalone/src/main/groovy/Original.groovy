
import grails.orm.bootstrap.*
import grails.persistence.*
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.h2.Driver


init = new HibernateDatastoreSpringInitializer(Person)
def dataSource = new DriverManagerDataSource(Driver.name, "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE", 'sa', '')
init.configureForDataSource(dataSource)


println "Total people = " + Person.count()


@Entity
class Person {
    String name
    static constraints = {
        name blank:false
    }
}