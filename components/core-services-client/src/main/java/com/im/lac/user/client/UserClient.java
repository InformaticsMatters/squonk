package com.im.lac.user.client;

import org.squonk.core.user.User;
import org.squonk.types.io.JsonHandler;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Client for ServiceDescriptors.
 *
 * @author timbo
 */
public class UserClient {

    private static final Logger LOG = Logger.getLogger(UserClient.class.getName());

    private static final String DEFAULT_BASE_URL = "http://demos.informaticsmatters.com:8080/coreservices/rest/v1/users/";

    private final String base;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final JsonHandler jsonHandler = new JsonHandler();

    public UserClient(String baseUrl) {
        this.base = baseUrl;
    }

    public UserClient() {
        base = DEFAULT_BASE_URL;
    }

    /**
     * Get the User object associated with this username
     *
     * @param username Username of the authenticated user
     * @return The user object
     * @throws java.io.IOException
     */
    public User getUserObject(String username) throws IOException {
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        
        String uri = base + username;
        LOG.log(Level.FINE, "Requesting user with {0}", uri);
        HttpGet httpGet = new HttpGet(uri);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOG.log(Level.WARNING, "Request for {0} failed: {1}", new Object[]{uri,json});
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            LOG.log(Level.FINE, "User JSON: {0}", json);
            return jsonHandler.objectFromJson(json, User.class);
        }
    }

}
