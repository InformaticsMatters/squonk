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

package org.squonk.core.client;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.squonk.client.UserClient;
import org.squonk.core.config.SquonkClientConfig;
import org.squonk.core.user.User;
import org.squonk.types.io.JsonHandler;

import javax.enterprise.inject.Default;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for Users.
 * The primary perpose of this client is to allow the server to be ready for requests for this user
 * e.g. create the user in the database etc..
 *
 * @author timbo
 */
@Default
public class UserRestClient implements UserClient, Serializable {

    private static final Logger LOG = Logger.getLogger(UserRestClient.class.getName());

    private final String baseUrl;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();

    public UserRestClient(String serverUrl) {
        this.baseUrl = serverUrl + SquonkClientConfig.CORE_SERVICES_PATH + "/users";
        LOG.info("UserRestClient using base URL of " + baseUrl);
    }

    public UserRestClient() {
        this(SquonkClientConfig.CORE_SERVICES_SERVER);
    }

    /**
     * Get the User object associated with this username
     *
     * @param username Username of the authenticated user
     * @return The user object
     * @throws java.io.IOException
     */
    public User getUser(String username) throws IOException {
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        
        String uri = baseUrl + "/" + username;
        LOG.log(Level.FINE, "Requesting user with {0}", uri);
        HttpGet httpGet = new HttpGet(uri);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOG.log(Level.WARNING, "Request for {0} failed: {1}", new Object[]{uri,response.getStatusLine().toString()});
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            LOG.log(Level.FINE, "User JSON: {0}", json);
            return JsonHandler.getInstance().objectFromJson(json, User.class);
        }
    }

}
