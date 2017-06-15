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

package org.squonk.core.service.user;

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.client.UserClient
import org.squonk.core.user.User

import javax.sql.DataSource

/**
 *
 * @author timbo
 */
@Log
public class UserPostgresClient implements UserClient {
    
    private final DataSource dataSource

    
    public UserPostgresClient(DataSource dataSource) {
        this.dataSource = dataSource
    }
    
    public User getUser(String username) {
        checkUserExists(username)
        return fetchUser(username)
    }
    
    private void checkUserExists(String username) {
        Sql db = new Sql(dataSource.connection)
        try {
            int count = db.firstRow("SELECT count(*) from users.users WHERE username = $username")[0]
            if (count == 0) {
                log.info("Adding user $username")
                db.executeInsert("INSERT INTO users.users (username) VALUES ($username)")
            }
        } finally {
            db.close()
        }
    }
    
    private User fetchUser(String username) {
        Sql db = new Sql(dataSource.connection)
        try {
            def data = db.firstRow("SELECT id, username from users.users WHERE username = $username AND active = 1")
            if (data == null) {
                throw new IllegalStateException("User does not exist or is inactived")
            } 
            return new User(data.id, data.username)
        } finally {
            db.close()
        }
    }
    
}
