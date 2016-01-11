package com.im.lac.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 01/01/16.
 */
public class AbstractHttpClient {

    private static final Logger LOG = Logger.getLogger(AbstractHttpClient.class.getName());
    protected final CloseableHttpClient httpclient = HttpClients.createDefault();

    protected String executeGetAsString(URIBuilder b) throws IOException {
        return executeGetAsString(b, new NameValuePair[0]);
    }

    protected InputStream executeGetAsInputStream(URIBuilder b) throws IOException {
        return executeGetAsInputStream(b, new NameValuePair[0]);
    }

    protected String executeGetAsString(URIBuilder b, NameValuePair... headers) throws IOException {
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
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI. Realy?", e);
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
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI. Realy?", e);
        }
    }

    protected InputStream executePostAsInputStream(URIBuilder b, String body, NameValuePair... headers) throws IOException {
        try {
            return executePostAsInputStream(b, body == null ? null : new StringEntity(body), headers);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported Encoding. Realy?", e);
        }
    }

    protected InputStream executePostAsInputStream(URIBuilder b, AbstractHttpEntity body, NameValuePair... headers) throws IOException {
        try {
            HttpPost httpPost = new HttpPost(b.build());
            if (headers != null && headers.length > 0) {
                addHeaders(httpPost, headers);
            }
            if (body != null) {
                LOG.finer("Setting POST body: " + body);
                httpPost.setEntity(body);
            }
            CloseableHttpResponse response = httpclient.execute(httpPost);
            LOG.finer(response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() != 200) {
                String err = EntityUtils.toString(entity);
                LOG.log(Level.WARNING, "Request failed: {0}", err);
                throw new IOException("Request failed: " + response.getStatusLine().toString());
            }
            InputStream is = entity.getContent();
            return is;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Bad URI. Realy?", e);
        }
    }

//    protected InputStream executePostAsInputStream(URIBuilder b, InputStream body, NameValuePair... headers) {
//        try {
//            HttpPost httpPost = new HttpPost(b.build());
//            if (headers != null && headers.length > 0) {
//                addHeaders(httpPost, headers);
//            }
//            if (body != null) {
//                LOG.info("Setting POST body: " + body);
//                httpPost.setEntity(new InputStreamEntity(body));
//            }
//            CloseableHttpResponse response = httpclient.execute(httpPost);
//            LOG.info(response.getStatusLine().toString());
//            HttpEntity entity = response.getEntity();
//            if (response.getStatusLine().getStatusCode() != 200) {
//                String err = EntityUtils.toString(entity);
//                LOG.log(Level.WARNING, "Request failed: {0}", err);
//                throw new IOException("Request failed: " + response.getStatusLine().toString());
//            }
//            InputStream is = entity.getContent();
//            return is;
//        } catch (IOException e) {
//            throw new RuntimeException("IOException", e);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Bad URI. Realy?", e);
//        }
//    }

    protected void addHeaders(HttpMessage message, NameValuePair... headers) {
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
}
