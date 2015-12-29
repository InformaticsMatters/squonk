package com.im.lac.services.client;

import com.im.lac.services.CommonConstants;
import com.im.lac.services.ServiceDescriptor;
import org.squonk.types.io.JsonHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
public class ServicesClient {

    private static final Logger LOG = Logger.getLogger(ServicesClient.class.getName());

    private static final String DEFAULT_BASE_URL = "http://demos.informaticsmatters.com:8080/coreservices/rest/v1/services";

    private final String base;
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final JsonHandler jsonHandler = new JsonHandler();

    public ServicesClient(String baseUrl) {
        this.base = baseUrl;
    }

    public ServicesClient() {
        base = DEFAULT_BASE_URL;
    }

    /**
     * Get an List all the known ServiceDescriptorSets
     *
     * @param username Username of the authenticated user
     * @return A list of job statuses matching the filters
     * @throws java.io.IOException
     */
    public List<ServiceDescriptor> getServiceDefinitions(String username) throws IOException {
        if (username == null) {
            throw new IllegalStateException("Username must be specified");
        }
        HttpGet httpGet = new HttpGet(base);
        httpGet.setHeader(CommonConstants.HEADER_SQUONK_USERNAME, username);
        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            LOG.fine(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200) {
                String err = EntityUtils.toString(entity);
                LOG.warning("Request failed: " + err);
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            InputStream is = entity.getContent();
            return jsonHandler.streamFromJson(is, ServiceDescriptor.class, true).collect(Collectors.toList());
        }
    }

}
