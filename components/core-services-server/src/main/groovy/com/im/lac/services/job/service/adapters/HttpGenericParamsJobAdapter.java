package com.im.lac.services.job.service.adapters;

import com.im.lac.camel.util.CamelUtils;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.camel.CamelContext;

/**
 * HTTP adapter that handles header and query params in a "best effort" manner.
 * Parameter whose name starts with "query." are treated as a query parameter
 * and those starting with "header." are treated as header parameters.
 *
 * @author timbo
 */
public class HttpGenericParamsJobAdapter extends SimpleHttpJobAdapter {

    private static final Logger LOG = Logger.getLogger(HttpGenericParamsJobAdapter.class.getName());

    @Override
    public InputStream submit(
            CamelContext context,
            String endpoint,
            InputStream input,
            Map<String, Object> params) throws Exception {

        return CamelUtils.doPostUsingHeadersAndQueryParams(context, endpoint, input, params);
    }
}
