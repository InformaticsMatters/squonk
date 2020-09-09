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

package org.squonk.camel.util;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.squonk.api.MimeTypeResolver;
import org.squonk.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author timbo
 */
public class CamelUtils {

    private static final Logger LOG = Logger.getLogger(CamelUtils.class.getName());

    private static final JsonHandler jsonHandler = new JsonHandler();
    // TODO - its probably better to get the thread pool from CamelContext?
    final static ExecutorService executor = Executors.newCachedThreadPool();



//    public static void handleMoleculeObjectStreamInput(Exchange exch) throws IOException, ClassNotFoundException {
//
//        InputStream maybeGzippedInputStream = exch.getIn().getBody(InputStream.class);
//        final InputStream is = IOUtils.getGunzippedInputStream(maybeGzippedInputStream);
//
//        // TODO - grab the metadata from the header if present
//        Metadata meta = new Metadata(MoleculeObject.class.getName(), Metadata.Type.STREAM, 0);
//        Stream<MoleculeObject> stream = (Stream<MoleculeObject>) jsonHandler.unmarshalItemsAsStream(meta, is);
//        StreamProvider sp = new SimpleStreamProvider<>(stream.onClose(() -> {
//            LOG.fine("Stream closed. Closing underlying InputStream");
//            IOUtils.close(is);
//        }), MoleculeObject.class);
//        exch.getIn().setBody(sp);
//    }
//
//    public static void handleMoleculeObjectStreamOutput(Exchange exch) throws IOException {
//        String acceptEncoding = exch.getIn().getHeader("Accept-Encoding", String.class);
//        // TODO - handle other formats like SDF is the Accept header is set.
//        boolean gzip = acceptEncoding == null ? false : "gzip".equals(acceptEncoding.toLowerCase());
//        LOG.fine("GZIP: " + gzip);
//        Stream<MoleculeObject> mols = StreamingMoleculeObjectSourcer.bodyAsMoleculeObjectStream(exch);
//        InputStream is = jsonHandler.marshalStreamToJsonArray(mols,  gzip);
//        exch.getIn().setBody(is);
//    }

    public static int putPropertiesAsHeaders(Message message, File propertiesFile) throws FileNotFoundException, IOException {
        try (InputStream is = new FileInputStream(propertiesFile)) {
            Properties props = new Properties();
            props.load(is);
            return putPropertiesAsHeaders(message, props);
        }
    }

    public static int putPropertiesAsHeaders(Message message, Properties properties) {
        int count = 0;
        for (String pn : properties.stringPropertyNames()) {
            count++;
            Object v = properties.getProperty(pn);
            LOG.log(Level.INFO, "Setting header {0} to {1}", new Object[]{pn, v});
            message.setHeader(pn, v);
        }
        return count;
    }

    public static InputStream doRequestUsingHeadersAndQueryParams(
            CamelContext context,
            String method,
            String endpoint,
            InputStream input,
            Map<String, Object> requestHeaders,
            Map<String, Object> responseHeaders,
            Map<String, Object> options) throws Exception {

        Map<String, Object> allHeaders = new HashMap<>(requestHeaders);
        allHeaders.put(Exchange.HTTP_METHOD, method);
        String url = generateUrlUsingHeadersAndQueryParams(endpoint, options, allHeaders);
        LOG.log(Level.INFO, "Generated URL: {0}", url);
        allHeaders.put(Exchange.HTTP_URI, url);
        LOG.info("Using headers: " + allHeaders.entrySet().stream()
                .map((e) -> e.getKey() + ": " + (e.getValue() == null ? "" : e.getValue().toString()))
                .collect(Collectors.joining(" ")));

        ProducerTemplate pt = context.createProducerTemplate();

        LOG.info("REQUEST starting");
        Exchange response = pt.request("http4:dummy", exch -> {
            exch.getIn().setHeaders(allHeaders);
            if (input != null) {
                exch.getIn().setBody(input);
            }
        });

        Exception ex = response.getException();
        if (ex != null) {
            if (ex instanceof HttpOperationFailedException) {
                HttpOperationFailedException hofe = (HttpOperationFailedException)ex;
                int code = hofe.getStatusCode();
                String body = hofe.getResponseBody();
                LOG.warning("Request failed: " + body);
                throw new IOException("HTTP ERROR: " + code);
            } else {
                throw new IOException("HTTP ERROR: cause unknown");
            }
        } else {
            Message msg = getMessage(response);
            InputStream result = msg.getBody(InputStream.class);
            if (responseHeaders != null) {
                responseHeaders.putAll(msg.getHeaders());
            }
            return result;
        }
    }

    public static MoleculeObject readMoleculeObjectFromBody(Exchange exch) throws IOException {
        Message msg = (exch.hasOut() ? exch.getOut() : exch.getIn());
        if (msg.getBody() == null) {
            return null;
        }
        return readMoleculeObject(msg.getBody(String.class), msg.getHeader(Exchange.CONTENT_TYPE, String.class));
    }

    public static MoleculeObject readMoleculeObject(String value, String contentType) throws IOException {
        if (value == null) {
            return null;
        }

        if (contentType == null || MimeTypeResolver.MIME_TYPE_MOLECULE_OBJECT_JSON.equals(contentType)) {
            return JsonHandler.getInstance().objectFromJson(value, MoleculeObject.class);
        } else if (MimeTypeResolver.MIME_TYPE_MDL_MOLFILE.equals(contentType)) {
            return new MoleculeObject(value, "mol");
        } else if (MimeTypeResolver.MIME_TYPE_DAYLIGHT_SMILES.equals(contentType)) {
            return new MoleculeObject(value, "smiles");
        } else if (MimeTypeResolver.MIME_TYPE_DAYLIGHT_SMARTS.equals(contentType)) {
            return new MoleculeObject(value, "smarts");
        } else {
            LOG.warning("Unrecognised Content-Type: " + contentType);
            return null;
        }
    }


    public static Message getMessage(Exchange exch) {
        return exch.hasOut() ? exch.getOut() : exch.getIn();
    }

    public static String generateUrlUsingHeadersAndQueryParams(String endpoint, Map<String, Object> options, Map<String, Object> headers) throws UnsupportedEncodingException, URISyntaxException {
        int qcount = 0;
        StringBuilder b = new StringBuilder();
        if (options != null) {
            for (Map.Entry<String, Object> e : options.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();
                if (key.startsWith("header.")) {
                    headers.put(e.getKey().substring(7), e.getValue());
                } else if (key.startsWith("query.")) {
                    if (qcount > 0) {
                        b.append("&");
                    }
                    qcount++;

                    if (val == null) {
                        b.append(e.getKey());
                    } else {
                        b.append(e.getKey().substring(6)).append("=").append(val.toString());
                    }
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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        executor.shutdown();
    }

}
