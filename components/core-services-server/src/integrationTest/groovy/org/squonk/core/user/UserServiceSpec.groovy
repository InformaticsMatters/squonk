package org.squonk.core.user

import com.im.lac.services.util.*
import groovy.sql.Sql
import org.squonk.core.util.TestUtils

import javax.sql.DataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class UserServiceSpec extends Specification {
    
    @Shared DataSource dataSource = TestUtils.createTestDataSource()
    @Shared UserService service = new UserService(dataSource) 
    
    void "1. test create user"() {
        setup:
        String username = "UserServiceSpec"
        Sql db = new Sql(dataSource.connection)
        db.executeUpdate("DELETE FROM users.users WHERE username = $username")
        
        when:
        int start = db.firstRow("SELECT count(*) from users.users")[0]
        User user = service.getUser(username)
        int finish = db.firstRow("SELECT count(*) from users.users")[0]
        
        then:
        finish == start + 1
        user != null
        user.username == username
        
        cleanup:
        db.close()
        
    }
	
}

