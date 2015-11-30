package com.im.lac.services.job.service.adapters;

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
 * HTTP adapter that sets params as HTTP headers
 *
 * @author timbo
 */
public class HttpQueryParamsJobAdapter extends SimpleHttpJobAdapter {

    private static final Logger LOG = Logger.getLogger(HttpQueryParamsJobAdapter.class.getName());

    @Override
    public InputStream submit(
            CamelContext context,
            String endpoint,
            InputStream input,
            Map<String, Object> params) throws Exception {

        String url = generateUrl(endpoint, params);

        ProducerTemplate pt = context.createProducerTemplate();
        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_URI, url);
        headers.put(Exchange.HTTP_METHOD, "POST");

        return pt.requestBodyAndHeaders("http4:dummy", input, headers, InputStream.class);
    }

    String generateUrl(String endpoint, Map<String, Object> params) throws UnsupportedEncodingException, URISyntaxException {
        int qcount = 0;
        StringBuilder b = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                if (qcount > 0) {
                    b.append("&");
                }
                qcount++;
                Object val = e.getValue();
                if (val == null) {
                    b.append(e.getKey());
                } else {
                    b.append(e.getKey()).append("=").append(val.toString());
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
