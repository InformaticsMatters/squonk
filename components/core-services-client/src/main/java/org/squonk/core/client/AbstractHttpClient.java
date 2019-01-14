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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 01/01/16.
 */
public class AbstractHttpClient implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(AbstractHttpClient.class.getName());
    protected transient final CloseableHttpClient httpclient;
    protected transient final CloseableHttpAsyncClient asyncHttpclient;
    private boolean closed = false;
    protected transient final PoolingHttpClientConnectionManager connectionManager;

    public AbstractHttpClient() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(4000)
                .setConnectTimeout(4000)
                .setSocketTimeout(10000)
                .build();

        connectionManager = new PoolingHttpClientConnectionManager();
        // Increase max total connection from the default of 20
        connectionManager.setMaxTotal(100);
        // Increase default max connection per route from the default of 2
        connectionManager.setDefaultMaxPerRoute(25);

        httpclient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).build();

        asyncHttpclient = HttpAsyncClients.createDefault();
        asyncHttpclient.start();
    }

    @Override
    public void close() throws Exception {
        if (httpclient != null) {
            httpclient.close();
        }
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

    protected CloseableHttpResponse execute(HttpUriRequest req) throws IOException {
        debugConnections(req.getMethod(), req.getURI());
        return httpclient.execute(req);
    }

    protected String executeGetAsString(URIBuilder b) throws IOException {
        return executeGetAsString(b, new NameValuePair[0]);
    }

    protected InputStream executeGetAsInputStream(URIBuilder b) throws IOException {
        return executeGetAsInputStream(b, new NameValuePair[0]);
    }

    protected void executeDelete(URIBuilder b, NameValuePair... headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("DELETE", uri);
            HttpDelete httpDelete = new HttpDelete(uri);
            if (headers != null && headers.length > 0) {
                addHeaders(httpDelete, headers);
            }
            try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
                LOG.fine(response.getStatusLine().toString());
                checkResponse(response);
            }
        } catch (URISyntaxException e) {
            throw new IOException("Bad URI. Really?", e);
        }
    }

    protected InputStream executeDeleteAsInputStream(URIBuilder b, NameValuePair... headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("DELETE", uri);
            HttpDelete httpDelete = new HttpDelete(uri);
            if (headers != null && headers.length > 0) {
                addHeaders(httpDelete, headers);
            }
            CloseableHttpResponse response = httpclient.execute(httpDelete);
            LOG.fine(response.getStatusLine().toString());
            checkResponse(response);
            HttpEntity entity = response.getEntity();
            return entity.getContent();

        } catch (URISyntaxException e) {
            throw new IOException("Bad URI. Really?", e);
        }
    }

    protected String executeGetAsString(URIBuilder b, NameValuePair... headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("GET", uri);
            HttpGet httpGet = new HttpGet(uri);
            if (headers != null && headers.length > 0) {
                addHeaders(httpGet, headers);
            }
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                LOG.fine(response.getStatusLine().toString());
                checkResponse(response);
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
        } catch (URISyntaxException e) {
            throw new IOException("Bad URI. Really?", e);
        }
    }

    /**
     * Get as InputStream. The returned InputStream MUST be closed when finished or else the underlying HTTP resources will
     * remain open.
     *
     * @param b
     * @return
     */
    protected InputStream executeGetAsInputStream(URIBuilder b, NameValuePair... headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("GET", uri);
            HttpGet httpGet = new HttpGet(uri);
            if (headers != null && headers.length > 0) {
                addHeaders(httpGet, headers);
            }
            CloseableHttpResponse response = httpclient.execute(httpGet);
            LOG.fine("Response: " + response.getStatusLine().toString());
            checkResponse(response);
            HttpEntity entity = response.getEntity();
            return entity.getContent();
        } catch (URISyntaxException e) {
            throw new IOException("Bad URI. Really?", e);
        }
    }

    protected InputStream executePostAsInputStream(URIBuilder b, String body, NameValuePair... requestHeaders) throws IOException {
        return executePostAsInputStream(b, body == null ? null : new StringEntity(body), requestHeaders);
    }

    protected InputStream executePostAsInputStream(URIBuilder b, AbstractHttpEntity body) throws IOException {
        return executePostAsInputStream(b, body, new NameValuePair[0]);
    }

    protected InputStream executePostAsInputStream(URIBuilder b, AbstractHttpEntity body, NameValuePair... requestHeaders) throws IOException {
        return executePostAsInputStream(b, body, requestHeaders == null ? null : requestHeaders, null);
    }

    protected InputStream executePostAsInputStream(URIBuilder b, AbstractHttpEntity body, NameValuePair[] requestHeaders, Map<String,String> responseHeaders) throws IOException {

        CloseableHttpResponse response = doPost(b, body, requestHeaders);
        LOG.fine("POST complete: " + response.getStatusLine().toString());
        checkResponse(response);
        if (responseHeaders != null) {
            Header[] headers = response.getAllHeaders();
            if (headers != null) {
                for (Header h : headers) {
                    responseHeaders.put(h.getName(), h.getValue());
                }
            }
        }
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        return is;
    }

    protected InputStream executePutAsInputStream(URIBuilder b, AbstractHttpEntity body) throws IOException {
        return executePutAsInputStream(b, body, new NameValuePair[0]);
    }

    protected InputStream executePutAsInputStream(URIBuilder b, AbstractHttpEntity body, NameValuePair... headers) throws IOException {
        CloseableHttpResponse response = doPut(b, body, headers);
        LOG.fine(response.getStatusLine().toString());
        checkResponse(response);
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();
        return is;
    }

    protected void executePost(URIBuilder b, String body, NameValuePair... headers) throws IOException {
        executePost(b, body == null ? null : new StringEntity(body), headers);
    }

    protected void executePost(URIBuilder b, AbstractHttpEntity body) throws IOException {
        executePost(b, body, new NameValuePair[0]);
    }

    protected void executePost(URIBuilder b, AbstractHttpEntity body, NameValuePair... headers) throws IOException {
        try (CloseableHttpResponse response = doPost(b, body, headers)) {
            LOG.fine(response.getStatusLine().toString());
            checkResponse(response);
        }
    }

    protected CloseableHttpResponse doPost(URIBuilder b, AbstractHttpEntity body, NameValuePair... headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("POST", uri);
            LOG.fine("POSTing to " + uri);
            HttpPost httpPost = new HttpPost(uri);

            if (headers != null && headers.length > 0) {
                addHeaders(httpPost, headers);
            }
            if (body != null) {
                LOG.finer("Setting POST body: " + body);
                httpPost.setEntity(body);
            }
            return httpclient.execute(httpPost);
        } catch (URISyntaxException e) {
            throw new IOException("Bad URI. Really?", e);
        }
    }

    /** Streaming pose operation using AsyncHttpClient
     * Note this method is experimental but should eventually replace other POST and PUT operations as the standard
     * HttpClient is blocking and does not yield the response until the data is fully written.
     * Currently this method is only used for SDF conversion in the StructureIOClient class
     *
     * @param b
     * @param body
     * @param headers
     * @return
     * @throws IOException
     */
    protected InputStream executePostAsInputStreamStreaming(URIBuilder b, AbstractHttpEntity body, NameValuePair... headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("POST", uri);
            LOG.info("POSTing to " + uri);

            final HttpPost request = new HttpPost(uri);
            if (headers != null && headers.length > 0) {
                addHeaders(request, headers);
            }
            if (body != null) {
                LOG.finer("Setting POST body: " + body);
                request.setEntity(body);
            }
            LOG.fine("Posting commencing");
            final HttpAsyncRequestProducer producer = HttpAsyncMethods.create(request);
            final PipedInputStream pis = new PipedInputStream();
            final PipedOutputStream pout = new PipedOutputStream(pis);
            AsyncByteConsumer<HttpResponse> consumer = new AsyncByteConsumer<HttpResponse>() {

                private HttpResponse response;

                @Override
                protected void onByteReceived(ByteBuffer buf, IOControl ioctrl) throws IOException {
                    // ByteBuffer is a strange beast. Find out how it works before you make mistakes
                    // https://worldmodscode.wordpress.com/2012/12/14/the-java-bytebuffer-a-crash-course/

                    final byte[] bytes = new byte[buf.remaining()];
                    buf.duplicate().get(bytes);
                    LOG.fine("Received " + buf.toString() + " " + bytes.length);
                    pout.write(bytes);
                    pout.flush();
                }

                @Override
                protected void onResponseReceived(final HttpResponse response) {
                    this.response = response;
                    LOG.fine("Response received");
                }

                @Override
                protected HttpResponse buildResult(HttpContext context) throws Exception {
                    LOG.fine("Build Result");
                    IOUtils.close(pout);
                    return this.response;
                }

            };

            asyncHttpclient.execute(producer, consumer, new FutureCallback<HttpResponse>() {

                public void completed(final HttpResponse response) {
                    LOG.fine("Completed: " + request.getRequestLine() + "->" + response.getStatusLine());
                }

                public void failed(final Exception ex) {
                    LOG.info("Failed: " + request.getRequestLine() + "->" + ex);
                }

                public void cancelled() {
                    LOG.fine("Cancelled: "+ request.getRequestLine());
                }
            });

            return pis;

        } catch (URISyntaxException e1) {
            throw new IOException("Bad URI. Really?", e1);
        }

    }

    protected CloseableHttpResponse doPut(URIBuilder b, AbstractHttpEntity body, NameValuePair... headers) throws IOException {
        try {
            URI uri = b.build();
            debugConnections("PUT", uri);
            HttpPut httpPut = new HttpPut(uri);
            if (headers != null && headers.length > 0) {
                addHeaders(httpPut, headers);
            }
            if (body != null) {
                LOG.finer("Setting PUT body: " + body);
                httpPut.setEntity(body);
            }
            return httpclient.execute(httpPut);
        } catch (URISyntaxException e) {
            throw new IOException("Bad URI. Really?", e);
        }
    }

    protected void addHeaders(HttpMessage message, NameValuePair[] headers) {
        if (headers != null) {
            for (NameValuePair nvp : headers) {
                message.addHeader(nvp.getName(), nvp.getValue());
            }
        }
    }

    protected String toJson(Object o) {
        try {
            return JsonHandler.getInstance().objectToJson(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write JSON", e);
        }
    }

    protected <T> T fromJson(String json, Class<T> type) throws IOException {
        return JsonHandler.getInstance().objectFromJson(json, type);
    }

    protected <T> T fromJson(InputStream json, Class<T> type) throws IOException {
        try {
            return JsonHandler.getInstance().objectFromJson(json, type);
        } finally {
            IOUtils.close(json);
        }
    }

    /**
     * Throws IOException if response is not in the 200 range, providing whatever information is available as the
     * exception message.
     * Override this if you need different behaviour.
     *
     * @param response
     * @throws IOException
     */
    protected void checkResponse(HttpResponse response) throws IOException {

        if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            String err = null;
            if (is != null) {
                err = EntityUtils.toString(entity);
            }
            LOG.log(Level.WARNING, "Request failed: {0}", response.getStatusLine().toString());
            throw new IOException("Request failed: " + (err == null ? response.getStatusLine().toString() : err));
        }
    }
}
