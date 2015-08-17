package com.im.lac.services.job.service.adapters;

import com.im.lac.services.ServiceDescriptor;
import com.im.lac.services.job.service.AbstractDatasetJob;
import com.im.lac.services.job.service.AsyncJobRouteBuilder;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;


/**
 * HTTP adapter that handles header and query params in a "best effort" manner. Parameter whose name
 * starts with "query." are treated as a query parameter and those starting with "header." are
 * treated as header parameters.
 *
 * @author timbo
 */
public class HttpGenericParamsJobAdapter extends SimpleHttpJobAdapter {

    private static final Logger LOG = Logger.getLogger(HttpGenericParamsJobAdapter.class.getName());

    @Override
    InputStream submit(
            CamelContext context,
            AbstractDatasetJob job,
            ServiceDescriptor sd,
            String endpoint,
            InputStream input,
            Map<String, Object> params) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_METHOD, "POST");
        String url = generateUrl(endpoint, params, headers);
        LOG.info("Geenrated URL: " + url);
        headers.put(Exchange.HTTP_URI, url);

        ProducerTemplate pt = context.createProducerTemplate();

        return pt.requestBodyAndHeaders(AsyncJobRouteBuilder.ROUTE_HTTP_SUBMIT, input, headers, InputStream.class);
    }

    String generateUrl(String endpoint, Map<String, Object> params, Map<String, Object> headers) throws UnsupportedEncodingException, URISyntaxException {
        int qcount = 0;
        StringBuilder b = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                if (key.startsWith("header.")) {
                    headers.put(e.getKey().substring(7), e.getValue());
                } else if (key.startsWith("query.")) {
                    if (qcount > 0) {
                        b.append("&");
                    }
                    qcount++;

                    if (val == null) {
                        b.append(e.getKey());
                    } else {
                        b.append(e.getKey().substring(6)).append("=").append(val.toString());
                    }
                }
            }
        }

        String query = null;
        if (qcount > 0) {
            query = b.toString();
            LOG.log(Level.FINE, "Query: {0}", query);
        }

        URI base = new URI(endpoint);
        URI uri = new URI(base.getScheme(), null, base.getHost(), base.getPort(), base.getPath(), query, null);
        String encoded = uri.toString();

        LOG.log(Level.INFO, "URL: {0}", encoded);
        return encoded;
    }
}
