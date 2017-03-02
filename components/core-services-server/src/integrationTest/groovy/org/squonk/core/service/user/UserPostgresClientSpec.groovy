package org.squonk.core.service.user


import groovy.sql.Sql
import org.squonk.core.user.User
import org.squonk.core.util.TestUtils

import javax.sql.DataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class UserPostgresClientSpec extends Specification {
    
    @Shared DataSource dataSource = TestUtils.createTestSquonkDataSource()
    @Shared UserPostgresClient service = new UserPostgresClient(dataSource)
    
    void "1. test create user"() {
        setup:
        String username = "UserServiceSpec"
        Sql db = new Sql(dataSource.connection)
        db.executeUpdate("DELETE FROM users.users WHERE username = $username")
        
        when:
        int start = db.firstRow("SELECT count(*) from users.users")[0]
        User user1 = service.getUser(username)
        int middle = db.firstRow("SELECT count(*) from users.users")[0]
        User user2 =service.getUser(username)
        int end = db.firstRow("SELECT count(*) from users.users")[0]
        
        then:
        middle == start + 1
        middle == end
        user1 != null
        user1.username == username
        user2 != null
        user2.username == username

        
        cleanup:
        db.close()
        
    }
	
}

