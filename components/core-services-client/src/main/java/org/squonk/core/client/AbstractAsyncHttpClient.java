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

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.squonk.util.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Note this class is experimental but should eventually replace {@link AbstractHttpClient} as the standard Apache
 * HttpClient is blocking and does not yield the response until the data is fully written.
 * Currently this method is only used for SDF conversion in the StructureIOClient class
 *
 * Created by timbo on 01/01/16.
 */
public class AbstractAsyncHttpClient implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(AbstractAsyncHttpClient.class.getName());
    protected transient final CloseableHttpAsyncClient asyncHttpclient;
    private boolean closed = false;
    protected transient PoolingHttpClientConnectionManager connectionManager;

    public AbstractAsyncHttpClient() {

        try {
            ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
            PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
            connectionManager.setMaxTotal(100);
            connectionManager.setDefaultMaxPerRoute(25);

            // asyncHttpclient = HttpAsyncClients.createDefault();
            asyncHttpclient = HttpAsyncClients.custom()
                    .setConnectionManager(connectionManager)
                    .build();

            asyncHttpclient.start();
        } catch (IOException e) { ;
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (asyncHttpclient != null) {
            asyncHttpclient.close();
        }
        closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!closed) {
            close();
        }
    }

    protected void debugConnections(String method, URI uri) {
        if (LOG.isLoggable(Level.FINER)) {
            StringBuilder b = new StringBuilder("============================== POOL STATS FOR ")
                    .append(method)
                    .append(" ")
                    .append(uri.toString())
                    .append(" ==============================");
            b.append("\nTotal Stats: " + connectionManager.getTotalStats().toString());
            for (HttpRoute route : connectionManager.getRoutes()) {
                b.append("\nRoute Stats: " + route + " " + connectionManager.getStats(route).toString());
            }
            b.append("\n============================== POOL STATS END ================================\n");
            LOG.fine(b.toString());
        } else if (LOG.isLoggable(Level.FINE)) {
            LOG.info(method + " ----------> " + uri.toString());
        }
    }

    protected AsyncResponse executeGet(URIBuilder b, NameValuePair[] headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("GET", uri);
            LOG.fine("GETing to " + uri);

            HttpGet request = new HttpGet(uri);
            handleHeaders(request, headers);

            return executeAsync(request, headers);

        } catch (URISyntaxException e1) {
            throw new IOException("Bad URI. Really?", e1);
        }
    }

    /**
     * POST operation using AsyncHttpClient
     *
     * @param b
     * @param body
     * @param headers
     * @return
     * @throws IOException
     */
    protected AsyncResponse executePost(URIBuilder b,
                                        AbstractHttpEntity body,
                                        NameValuePair[] headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("POST", uri);
            LOG.fine("POSTing to " + uri);

            HttpPost request = new HttpPost(uri);
            handleHeaders(request, headers);
            handleBody(request, body);

            return executeAsync(request, headers);

        } catch (URISyntaxException e1) {
            throw new IOException("Bad URI. Really?", e1);
        }
    }

    protected AsyncResponse executePut(URIBuilder b,
                                       AbstractHttpEntity body,
                                       NameValuePair[] headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("PUT", uri);
            LOG.fine("PUTing to " + uri);

            HttpPut request = new HttpPut(uri);
            handleHeaders(request, headers);
            handleBody(request, body);

            return executeAsync(request, headers);

        } catch (URISyntaxException e1) {
            throw new IOException("Bad URI. Really?", e1);
        }
    }

    private void handleBody(HttpEntityEnclosingRequestBase request, AbstractHttpEntity body) {
        if (body != null) {
            LOG.finer("Setting request body: " + body);
            request.setEntity(body);
        }
    }

    private void handleHeaders(HttpRequestBase request, NameValuePair[] headers) {
        if (headers != null && headers.length > 0) {
            addHeaders(request, headers);
        }
    }


    private AsyncResponse executeAsync(HttpRequestBase request, NameValuePair[] headers) throws IOException {

        LOG.fine("Request commencing");
        final HttpAsyncRequestProducer producer = HttpAsyncMethods.create(request);
        AsyncResponseByteConsumer consumer = new AsyncResponseByteConsumer();
        asyncHttpclient.execute(producer, consumer, new AsyncResponseCallback(request, consumer.getOutputStream()));
        return consumer.getAsyncResponse();
    }

    protected void addHeaders(HttpMessage message, NameValuePair[] headers) {
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                message.addHeader(nvp.getName(), nvp.getValue());
            }
        }
    }

    class AsyncResponseCallback implements FutureCallback<HttpResponse> {

        private final HttpRequest request;
        private final OutputStream out;

        AsyncResponseCallback(HttpRequest request, OutputStream out) {
            this.request = request;
            this.out = out;
        }

        public void completed(final HttpResponse response) {
            IOUtils.close(out);
            LOG.fine("Completed: " + request.getRequestLine() + "->" + response.getStatusLine());
        }

        public void failed(final Exception ex) {
            IOUtils.close(out);
            LOG.info("Failed: " + request.getRequestLine() + "->" + ex);
        }

        public void cancelled() {
            IOUtils.close(out);
            LOG.info("Cancelled: " + request.getRequestLine());
        }
    }

    class AsyncResponseByteConsumer extends AsyncByteConsumer<HttpResponse> {

        private final CompletableFuture<HttpResponse> httpResponseFuture = new CompletableFuture<>();
        private final AsyncResponse asyncResponse;
        private final PipedInputStream pis;
        private final PipedOutputStream pout;


        AsyncResponseByteConsumer() throws IOException {
            this.pis = new PipedInputStream();
            this.pout = new PipedOutputStream(pis);
            this.asyncResponse = new AsyncResponse(pis, httpResponseFuture);
        }

        InputStream getInputStream() {
            return pis;
        }

        OutputStream getOutputStream() {
            return pout;
        }

        Future<HttpResponse> getHttpResponse() {
            return httpResponseFuture;
        }

        AsyncResponse getAsyncResponse() {
            return asyncResponse;
        }

        @Override
        protected void onByteReceived(ByteBuffer buf, IOControl ioctrl) throws IOException {
            // ByteBuffer is a strange beast. Find out how it works before you make mistakes.
            // https://worldmodscode.wordpress.com/2012/12/14/the-java-bytebuffer-a-crash-course/

            final byte[] bytes = new byte[buf.remaining()];
            buf.duplicate().get(bytes);
            pout.write(bytes);
            pout.flush();
            //LOG.fine("Received " + buf.toString() + " " + bytes.length);
        }

        @Override
        protected void onResponseReceived(final HttpResponse response) {
            this.httpResponseFuture.complete(response);
            LOG.fine("Response received");
        }

        @Override
        protected HttpResponse buildResult(HttpContext context) throws Exception {
            LOG.fine("Build Result");
            return this.httpResponseFuture.get();
        }
    }

    class AsyncResponse implements AutoCloseable {

        private final InputStream inputStream;
        private final Future<HttpResponse> httpResponse;

        AsyncResponse(InputStream inputStream, Future<HttpResponse> httpResponse) {
            this.inputStream = inputStream;
            this.httpResponse = httpResponse;
        }

        protected InputStream getInputStream() {
            return inputStream;
        }

        protected Future<HttpResponse> getHttpResponse() {
            return httpResponse;
        }

        @Override
        public void close() {
            IOUtils.close(inputStream);
        }
    }

    public static String readFirstheader(HttpResponse response, String name) {
        Header h = response.getFirstHeader(name);
        return h == null ? null : h.getValue();
    }

    /**
     * Throws IOException if response is not in the 200 range, providing whatever information is available as the
     * exception message.
     * Override this if you need different behaviour.
     *
     * @param asyncResponse
     * @throws IOException
     */
    public static void checkResponse(AsyncResponse asyncResponse) throws Exception {

        HttpResponse httpResponse = asyncResponse.getHttpResponse().get();
        LOG.fine("Response: " + httpResponse.getStatusLine().toString());

        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {

            InputStream is = asyncResponse.getInputStream();
            String err = IOUtils.convertStreamToString(is);
            LOG.log(Level.WARNING, "Request failed: {0}", httpResponse.getStatusLine().toString());
            throw new IOException("Request failed: " + (err == null ? httpResponse.getStatusLine().toString() : err));
        }
    }

}
