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

import org.squonk.core.ServerConstants;
import org.squonk.core.user.User;
import org.squonk.core.util.Utils;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

/**
 *
 * @author timbo
 */
public class UserHandler {

    private final UserPostgresClient service;

    public UserHandler(UserPostgresClient service) {
        this.service = service;
    }

    static UserHandler getUserHandler(Exchange exch) {
        return getUserHandler(exch.getContext());
    }

    static UserHandler getUserHandler(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(ServerConstants.USER_HANDLER, UserHandler.class);
    }

    public static void putUser(Exchange exch) {
        UserHandler h = getUserHandler(exch);
        String username = Utils.fetchUsername(exch);
        User user = h.fetchUser(username);
        exch.getIn().setBody(user);
    }

    public User fetchUser(String username) {
        return service.getUser(username);
    }

}
