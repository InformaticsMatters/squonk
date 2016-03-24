package org.squonk.http;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.squonk.api.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by timbo on 23/03/2016.
 */
public class DefaultHttpExecutor<S,T> implements HttpExecutor {

    private final CamelContext context;
    private final HttpMethods method;
    private final URI uri;
    private final NameValuePair[] basicHeaders;
    private final List<NameValuePair> extraHeaders = new ArrayList<>();
    private InputStream body;
    private Exchange result;

    public DefaultHttpExecutor(
            CamelContext context,
            HttpMethods method,
            URI uri,
            NameValuePair... headers) {
        this.context = context;
        this.method = method;
        this.uri = uri;
        this.basicHeaders = headers;
    }

    @Override
    public void addRequestHeader(String name, String value) {
        extraHeaders.add(new BasicNameValuePair(name, value));
    }

    @Override
    public void setRequestBody(InputStream is) {
        this.body = is;
    }

    @Override
    public void execute() throws IOException {
        ProducerTemplate pt = context.createProducerTemplate();
        Map<String,Object> headers = new LinkedHashMap<>();
        for (NameValuePair h : extraHeaders) {
            headers.put(h.getName(), h.getValue());
        }
        if (basicHeaders != null) {
            for (NameValuePair h : basicHeaders) {
                headers.put(h.getName(), h.getValue());
            }
        }

        headers.put(Exchange.HTTP_METHOD, method.toString());
        headers.put(Exchange.HTTP_URI, uri.toString());

        result = pt.request("http4://willbeignored", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeaders(headers);
                exchange.getIn().setBody(body);
            }
        });

    }

    @Override
    public InputStream getResponseBody() {
        if (result == null) {
            throw new IllegalStateException("execute() must be called before results can be obtained");
        }
        return result.getOut().getBody(InputStream.class);
    }

    @Override
    public String getResponseHeader(String name) {
        if (result == null) {
            throw new IllegalStateException("execute() must be called before results can be obtained");
        }
        return result.getOut().getHeader(name, String.class);
    }
}
