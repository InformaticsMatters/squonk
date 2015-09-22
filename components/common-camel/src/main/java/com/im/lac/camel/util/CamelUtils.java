package com.im.lac.camel.util;

import com.im.lac.camel.processor.StreamingMoleculeObjectSourcer;
import com.im.lac.dataset.Metadata;
import com.im.lac.types.MoleculeObject;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
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
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

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

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        executor.shutdown();
    }

}
