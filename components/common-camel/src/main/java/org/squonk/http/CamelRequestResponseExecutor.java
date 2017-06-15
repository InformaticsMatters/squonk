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

package org.squonk.http;

import org.apache.camel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by timbo on 23/03/2016.
 */
public class CamelRequestResponseExecutor<S,T> implements RequestResponseExecutor {

    private final CamelContext context;
    private final String route;
    private final Map<String,Object> headers = new LinkedHashMap<>();
    private InputStream body;
    private Exchange exchange;

    public CamelRequestResponseExecutor(CamelContext context, String route) {
        this.context = context;
        this.route = route;
    }

    public CamelRequestResponseExecutor(Exchange exchange) {
        this.exchange = exchange;
        context = exchange.getContext();
        route = null;
    }

    @Override
    public void prepareRequestHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void prepareRequestBody(InputStream is) {
        this.body = is;
    }

    @Override
    public void setResponseHeader(String name, String value) {
        if (exchange == null) {
            throw new IllegalStateException("Exchange must exist before results can be obtained");
        }
        getMessage().setHeader(name, value);
    }

    @Override
    public void setResponseBody(InputStream is) {
        if (exchange == null) {
            throw new IllegalStateException("Exchange must exist before results can be obtained");
        }
        getMessage().setBody(is);
    }

    @Override
    public void execute() throws IOException {

        ProducerTemplate pt = context.createProducerTemplate();
        exchange = pt.request(route, new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeaders(headers);
                exchange.getIn().setBody(body);
            }
        });
    }

    @Override
    public InputStream getResponseBody() {
        if (exchange == null) {
            throw new IllegalStateException("execute() must be called before results can be obtained");
        }
        return getMessage().getBody(InputStream.class);
    }

    @Override
    public String getResponseHeader(String name) {
        if (exchange == null) {
            throw new IllegalStateException("Request must be executed before results can be obtained");
        }
        return getMessage().getHeader(name, String.class);
    }

    public Exchange getExchange() {
        return exchange;
    }

    public CamelContext getCamelContext() {
        return context;
    }

    protected Message getMessage() {
        return exchange.hasOut() ? exchange.getOut() : exchange.getIn();
    }
}
