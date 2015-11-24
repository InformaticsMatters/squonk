package com.im.lac.services.job.service.adapters;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;

/**
 * HTTP adapter that sets params as HTTP headers
 *
 * @author timbo
 */
public class HttpHeadersJobAdapter extends SimpleHttpJobAdapter {

    @Override
    public InputStream submit(
            CamelContext context,
            String endpoint,
            InputStream input,
            Map<String, Object> params) throws Exception {

        ProducerTemplate pt = context.createProducerTemplate();
        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_URI, endpoint);
        headers.put(Exchange.HTTP_METHOD, "POST");
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                headers.put(e.getKey(), e.getValue());
            }
        }

        return pt.requestBodyAndHeaders("http4:dummy", input, headers, InputStream.class);
    }
}
