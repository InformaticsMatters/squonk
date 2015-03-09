package com.im.lac.camel.processor;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.InputStream;
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

    public static Stream<MoleculeObject> bodyAsMoleculeObjectStream(Exchange exchange) {

        Object body = exchange.getIn().getBody();
        LOG.log(Level.FINE, "Body is {0}", body.getClass().getName());

        if (body instanceof Stream) {
            return (Stream<MoleculeObject>) body;
        }
        if (body instanceof Iterable) {
            Iterable<MoleculeObject> moliter = (Iterable<MoleculeObject>) body;
            Spliterator spliterator = Spliterators.spliteratorUnknownSize(moliter.iterator(), Spliterator.NONNULL | Spliterator.ORDERED);
            return StreamSupport.stream(spliterator, false);
        } else {
            if (body instanceof Iterator) {
                Iterator<MoleculeObject> iterator = (Iterator<MoleculeObject>) body;
                Spliterator spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL | Spliterator.ORDERED);
                return StreamSupport.stream(spliterator, false);
            } else {
                if (body instanceof InputStream) {
                    MoleculeObjectIterable moliter = exchange.getContext().getTypeConverter().convertTo(MoleculeObjectIterable.class, body);
                    Spliterator spliterator = Spliterators.spliteratorUnknownSize(moliter.iterator(), Spliterator.NONNULL | Spliterator.ORDERED);
                    return StreamSupport.stream(spliterator, false);
                }
            }
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
