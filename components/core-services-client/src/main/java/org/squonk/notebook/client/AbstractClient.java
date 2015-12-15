package org.squonk.notebook.client;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author simetrias
 */
public abstract class AbstractClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class.getName());
    private Client client;

    protected WebResource.Builder newResourceBuilder(String context, MultivaluedMap<String, String> queryParams) {
        String uriString = getServiceBaseUri() + context;
        UriBuilder builder = UriBuilder.fromUri(uriString);
        if (queryParams != null) {
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                for (String value : entry.getValue()) {
                    builder = UriBuilder.fromUri(builder.queryParam(entry.getKey(), "{value}").build(value));
                }
            }
        }

        URI uri = builder.build();

        logger.debug(uriString);

        if (client == null) {
            prepareClient();
        }
        WebResource resource = client.resource(uri);
        return resource.getRequestBuilder();
    }

    protected WebResource.Builder newResourceBuilder(String context) {
        return newResourceBuilder(context, null);
    }

    protected synchronized void prepareClient() {
        if (client == null) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getClasses().add(JacksonJsonProvider.class);
            client = Client.create(clientConfig);
            client.setChunkedEncodingSize(4096);
        }
    }

    protected abstract String getServiceBaseUri();


}
