/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

