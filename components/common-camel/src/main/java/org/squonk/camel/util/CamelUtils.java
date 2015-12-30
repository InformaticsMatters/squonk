package org.squonk.camel.util;

import org.squonk.camel.processor.StreamingMoleculeObjectSourcer;
import com.im.lac.dataset.Metadata;
import com.im.lac.types.MoleculeObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;
import com.im.lac.util.SimpleStreamProvider;
import com.im.lac.util.StreamProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;

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
        Metadata meta = new Metadata(MoleculeObject.class.getName(), Metadata.Type.ARRAY, 0);
        Stream<MoleculeObject> stream = (Stream<MoleculeObject>) jsonHandler.unmarshalItemsAsStream(meta, is);
        StreamProvider sp = new SimpleStreamProvider<>(stream.onClose(() -> {
            IOUtils.close(is);
        }), MoleculeObject.class);
        exch.getIn().setBody(sp);
    }

    public static void handleMoleculeObjectStreamOutput(Exchange exch) throws IOException {
        //String accept = exch.getIn().getHeader("Accept", String.class);
        // TODO - handle other formats like SDF is the Accept header is set.
        boolean gzip = "gzip".equals(exch.getIn().getHeader("Accept-Encoding", String.class));
        final PipedInputStream pis = new PipedInputStream();
        final OutputStream pout = new PipedOutputStream(pis);
        Stream<MoleculeObject> mols = StreamingMoleculeObjectSourcer.bodyAsMoleculeObjectStream(exch);
        Metadata meta = new Metadata(MoleculeObject.class.getName(), Metadata.Type.ARRAY, 0);
        final OutputStream out = (gzip ? new GZIPOutputStream(pout) : pout);
        final Stream<MoleculeObject> molsClose = mols.onClose(() -> IOUtils.close(out));
        exch.getIn().setBody(pis);

        Callable c = (Callable) () -> {
            jsonHandler.marshalItems(molsClose, meta, out);
            // HOW to handle metadata? Its only complete once the stream is processed so can't be a header
            //String metaJson = jsonHandler.objectToJson(meta);
            //exch.getIn().setHeader(Constants.HEADER_METADATA, metaJson);

            return true;

        };
        executor.submit(c);

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

    public static InputStream doPostUsingHeadersAndQueryParams(
            CamelContext context,
            String endpoint,
            InputStream input,
            Map<String, Object> params) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put(Exchange.HTTP_METHOD, "POST");
        String url = generateUrlUsingHeadersAndQueryParams(endpoint, params, headers);
        LOG.log(Level.INFO, "Generated URL: {0}", url);
        headers.put(Exchange.HTTP_URI, url);

        ProducerTemplate pt = context.createProducerTemplate();

        return pt.requestBodyAndHeaders("http4:dummy", input, headers, InputStream.class);
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
