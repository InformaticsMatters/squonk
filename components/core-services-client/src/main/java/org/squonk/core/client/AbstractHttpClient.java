package org.squonk.core.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 01/01/16.
 */
public class AbstractHttpClient {

    private static final Logger LOG = Logger.getLogger(AbstractHttpClient.class.getName());
    protected transient final CloseableHttpClient httpclient;
    protected transient final PoolingHttpClientConnectionManager connectionManager;
    protected boolean debugConnections = false;

    public AbstractHttpClient() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(4000)
                .setConnectTimeout(4000)
                .setSocketTimeout(4000).build();

        connectionManager = new PoolingHttpClientConnectionManager();
        // Increase max total connection from the default of 20
        //connectionManager.setMaxTotal(200);
        // Increase default max connection per route from the default of 2
        connectionManager.setDefaultMaxPerRoute(10);

        httpclient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).build();
    }

    protected void debugConnections(String method, URI uri) {
        if (debugConnections) {
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
            LOG.info(b.toString());
        } else {
            LOG.info(method + " ----------> " + uri.toString());
        }
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
                LOG.finer(response.getStatusLine().toString());
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
            LOG.finer(response.getStatusLine().toString());
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
                LOG.finer(response.getStatusLine().toString());
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
            ;
            HttpGet httpGet = new HttpGet(uri);
            if (headers != null && headers.length > 0) {
                addHeaders(httpGet, headers);
            }
            CloseableHttpResponse response = httpclient.execute(httpGet);
            LOG.finer(response.getStatusLine().toString());
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
        LOG.finer(response.getStatusLine().toString());
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
        LOG.finer(response.getStatusLine().toString());
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
            LOG.finer(response.getStatusLine().toString());
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

//    protected boolean responseOK(HttpResponse response) {
//        return response.getStatusLine().getStatusCode() >=200 && response.getStatusLine().getStatusCode() < 300;
//    }

    /**
     * Throws IOException if response is not in the 200 range, providing whatever information is available as the exception message.
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
            LOG.log(Level.WARNING, "Request failed: {0}", err);
            throw new IOException("Request failed: " + (err == null ? response.getStatusLine().toString() : err));
        }
    }
}
