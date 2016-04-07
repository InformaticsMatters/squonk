package org.squonk.camel.util;

import com.im.lac.dataset.Metadata;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.SimpleStreamProvider;
import com.im.lac.util.StreamProvider;
import org.apache.camel.*;
import org.squonk.camel.processor.StreamingMoleculeObjectSourcer;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import javax.servlet.http.HttpServlet;
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
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class CamelUtils {

    private static final Logger LOG = Logger.getLogger(CamelUtils.class.getName());

    private static final JsonHandler jsonHandler = new JsonHandler();
    // TODO - its probably better to get the thread pool from CamelContext?
    final static ExecutorService executor = Executors.newCachedThreadPool();



    public static void handleMoleculeObjectStreamInput(Exchange exch) throws IOException, ClassNotFoundException {

        InputStream maybeGzippedInputStream = exch.getIn().getBody(InputStream.class);
        final InputStream is = IOUtils.getGunzippedInputStream(maybeGzippedInputStream);

        // TODO - grab the metadata from the header if present
        Metadata meta = new Metadata(MoleculeObject.class.getName(), Metadata.Type.STREAM, 0);
        Stream<MoleculeObject> stream = (Stream<MoleculeObject>) jsonHandler.unmarshalItemsAsStream(meta, is);
        StreamProvider sp = new SimpleStreamProvider<>(stream.onClose(() -> {
            LOG.fine("Stream closed. Closing underlying InputStream");
            IOUtils.close(is);
        }), MoleculeObject.class);
        exch.getIn().setBody(sp);
    }

    public static void handleMoleculeObjectStreamOutput(Exchange exch) throws IOException {
        String acceptEncoding = exch.getIn().getHeader("Accept-Encoding", String.class);
        // TODO - handle other formats like SDF is the Accept header is set.
        boolean gzip = acceptEncoding == null ? false : "gzip".equals(acceptEncoding.toLowerCase());
        LOG.fine("GZIP: " + gzip);
        Stream<MoleculeObject> mols = StreamingMoleculeObjectSourcer.bodyAsMoleculeObjectStream(exch);
        InputStream is = jsonHandler.marshalStreamToJsonArray(mols,  gzip);
        exch.getIn().setBody(is);
    }

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


//    public static InputStream doRequestUsingHeadersAndQueryParams(
//            CamelContext context,
//            String method,
//            String endpoint,
//            InputStream input,
//            Map<String, Object> headers,
//            Map<String, Object> queryParams) throws Exception {
//
//        Map<String, Object> allHeaders = new HashMap<>(headers);
//        allHeaders.put(Exchange.HTTP_METHOD, method);
//        String url = generateUrlUsingHeadersAndQueryParams(endpoint, queryParams, allHeaders);
//        LOG.log(Level.INFO, "Generated URL: {0}", url);
//        allHeaders.put(Exchange.HTTP_URI, url);
//
//        ProducerTemplate pt = context.createProducerTemplate();
//
//        LOG.info("REQUEST starting");
//        InputStream result = pt.requestBodyAndHeaders("http4:dummy", input, allHeaders, InputStream.class);
//        LOG.info("REQUEST complete");
//        return result;
//    }

    public static InputStream doRequestUsingHeadersAndQueryParams(
            CamelContext context,
            String method,
            String endpoint,
            InputStream input,
            Map<String, Object> requestHeaders,
            Map<String, Object> responseHeaders,
            Map<String, Object> queryParams) throws Exception {

        Map<String, Object> allHeaders = new HashMap<>(requestHeaders);
        allHeaders.put(Exchange.HTTP_METHOD, method);
        String url = generateUrlUsingHeadersAndQueryParams(endpoint, queryParams, allHeaders);
        LOG.log(Level.INFO, "Generated URL: {0}", url);
        allHeaders.put(Exchange.HTTP_URI, url);

        ProducerTemplate pt = context.createProducerTemplate();

        LOG.info("REQUEST starting");
        //InputStream result = pt.requestBodyAndHeaders("http4:dummy", input, allHeaders, InputStream.class);
        Exchange response = pt.request("http4:dummy", exch -> {
            exch.getIn().setHeaders(allHeaders);
            exch.getIn().setBody(input);
        });
        LOG.info("REQUEST complete");
        Message msg = getMessage(response);
        InputStream result = msg.getBody(InputStream.class);
        if (responseHeaders != null) {
            responseHeaders.putAll(msg.getHeaders());
        }
        return result;
    }

    public static Message getMessage(Exchange exch) {
        return exch.hasOut() ? exch.getOut() : exch.getIn();
    }

    public static String generateUrlUsingHeadersAndQueryParams(String endpoint, Map<String, Object> params, Map<String, Object> headers) throws UnsupportedEncodingException, URISyntaxException {
        int qcount = 0;
        StringBuilder b = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
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
