package com.im.lac.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.squonk.types.io.JsonHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Created by timbo on 01/01/16.
 */
public class AbstractHttpClient {

    private static final Logger LOG = Logger.getLogger(AbstractHttpClient.class.getName());
    protected final CloseableHttpClient httpclient = HttpClients.createDefault();

    protected String executeGetAsString(URIBuilder b) {
        return executeGetAsString(b, null);
    }

    protected InputStream executeGetAsInputStream(URIBuilder b) {
        return executeGetAsInputStream(b, null);
    }

    protected String executeGetAsString(URIBuilder b, NameValuePair[] headers) {
        try {
            HttpGet httpGet = new HttpGet(b.build());
            if (headers != null && headers.length > 0) {
                addHeaders(httpGet, headers);
            }
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                LOG.finer(response.getStatusLine().toString());
                HttpEntity entity = response.getEntity();
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("HTTP GET failed: " + response.getStatusLine().toString());
                }
                return EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI. Realy?", e);
        }
    }

    /** Get as InputStream. The returned InputStream MUST be closed when finished or else the underlying HTTP resources will
     * remain open.
     *
     * @param b
     * @return
     */
    protected InputStream executeGetAsInputStream(URIBuilder b, NameValuePair[] headers) {
        try {
            HttpGet httpGet = new HttpGet(b.build());
            if (headers != null && headers.length > 0) {
                addHeaders(httpGet, headers);
            }
            CloseableHttpResponse response = httpclient.execute(httpGet);
            LOG.finer(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("HTTP GET failed: " + response.getStatusLine().toString());
            }
            return entity.getContent();
        } catch (IOException e) {
            throw new RuntimeException("IOException", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI. Realy?", e);
        }
    }

    protected void addHeaders(HttpMessage message, NameValuePair[] headers) {
        for (NameValuePair nvp : headers) {
            message.addHeader(nvp.getName(), nvp.getValue());
        }
    }

    protected <T> T fromJson(String json, Class<T> type) {
        try {
            return JsonHandler.getInstance().objectFromJson(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON", e);
        }
    }

    protected <T> T fromJson(InputStream json, Class<T> type) {
        try {
            return JsonHandler.getInstance().objectFromJson(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON", e);
        }
    }
}
