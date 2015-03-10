package com.im.lac.camel.processor;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.MoleculeObjectStreamProvider;
import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.camel.Exchange;

/**
 * Sources a Stream of MoleculeObjects from the exchange and dispatches them for
 * processing. Ideally the Exchanges IN message will contain a stream, but if it
 * contains an Iterator&lt;MoleculeObject&gt; or Iterable&lt;MoleculeObject&gt;
 * these are converted to a Stream using defaults (which may not be ideal).
 *
 * TODO - consider generalising this class to handle any instance type using
 * Generics
 *
 * @author Tim Dudgeon
 */
public abstract class StreamingMoleculeObjectSourcer {

    private static final Logger LOG = Logger.getLogger(StreamingMoleculeObjectSourcer.class.getName());

    public static Stream<MoleculeObject> bodyAsMoleculeObjectStream(Exchange exchange) throws IOException {

        LOG.log(Level.FINE, "Body is {0}", exchange.getIn().getBody().getClass().getName());

        MoleculeObjectStreamProvider sp = exchange.getIn().getBody(MoleculeObjectStreamProvider.class);
        if (sp != null) {
            return sp.getStream(true);
        }
        Stream stream = exchange.getIn().getBody(Stream.class);
        if (stream != null) {
            return stream;
        }

        Iterator<MoleculeObject> moiterator = null;
        MoleculeObjectIterable moi = exchange.getIn().getBody(MoleculeObjectIterable.class);
        if (moi != null) {
            moiterator = moi.iterator();
        } else {
            Iterator it = exchange.getIn().getBody(Iterator.class);
            if (it != null) {
                moiterator = it;
            } else {
                Iterable itb = exchange.getIn().getBody(Iterable.class);
                if (itb != null) {
                    moiterator = itb.iterator();
                }
            }
        }
        if (moiterator != null) {
            Spliterator spliterator = Spliterators.spliteratorUnknownSize(moiterator, Spliterator.NONNULL | Spliterator.ORDERED);
            return StreamSupport.stream(spliterator, true);
        }

        return null;
    }

    public void handle(Exchange exchange) throws Exception {
        MoleculeObject mol = exchange.getIn().getBody(MoleculeObject.class);
        if (mol != null) {
            handleSingle(exchange, mol);
            return;
        }
        Stream stream = bodyAsMoleculeObjectStream(exchange);
        if (stream != null) {
            handleMultiple(exchange, stream);
            return;
        }
        // give up
        handleOther(exchange, exchange.getIn().getBody());

    }

    public abstract void handleSingle(Exchange exchange, MoleculeObject mol) throws Exception;

    public abstract void handleMultiple(Exchange exchange, Stream<MoleculeObject> mols) throws Exception;

    public void handleOther(Exchange exchange, Object o) {
        LOG.log(Level.WARNING, "Can't find molecules from {0}", o.getClass().getName());
        throw new IllegalArgumentException("No valid MoleculeObject content could be found");
    }

}
