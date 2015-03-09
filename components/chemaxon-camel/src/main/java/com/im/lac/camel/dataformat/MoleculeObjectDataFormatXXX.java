package com.im.lac.camel.dataformat;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.CloseableMoleculeObjectQueue;
import com.im.lac.util.CloseableQueue;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.IOHelper;


// TODO This class should be moved to the common-camel module, but currently it can't 
// as the test requires the MoleculeObjectUtils class that is from this module.


/**
 * DataFormat that handles marshaling and unmarshaling of MoleculeObjects using Java
 * serialization.
 * Note: this will likely be replaced by (un)marshalling to JSON as that is faster 
 * and more compact.
 *
 * @author timbo
 */
public class MoleculeObjectDataFormatXXX implements DataFormat {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectDataFormatXXX.class.getName());

    private int marshalCount = 0;

    public int getMarshalCount() {
        return marshalCount;
    }

    private int unmarshalCount = 0;

    public int getUnmarshalCount() {
        return unmarshalCount;
    }

    @Override
    public void marshal(Exchange exchange, Object o, OutputStream out) throws IOException {
        marshal(o, out);
    }

    /**
     * Marshal the MoleculeObjects (as Iterator or Iterable) to the OutputStream
     * using Java serialization
     * 
     * @param o
     * @param out
     * @throws IOException 
     */
    public void marshal(Object o, OutputStream out) throws IOException {

        Iterator<MoleculeObject> mols = null;
        if (o instanceof MoleculeObjectIterable) {
            mols = ((MoleculeObjectIterable) o).iterator();
        } else if (o instanceof Stream) {
            mols = ((Stream<MoleculeObject>) o).iterator();
        } else if (o instanceof Iterator) {
            mols = (Iterator<MoleculeObject>) o;
        } else if (o instanceof Iterable) {
            mols = ((Iterable) o).iterator();
        } else if (o instanceof MoleculeObject) {
            mols = Collections.singletonList((MoleculeObject) o).iterator();
        } else {
            throw new IllegalArgumentException("Bad format. Can't handle " + o.getClass().getName());
        }

        ObjectOutputStream oos = new ObjectOutputStream(out);
        try {
            while (mols.hasNext()) {
                MoleculeObject mo = mols.next();
                oos.writeObject(mo);
                marshalCount++;
            }
        } finally {
            IOHelper.close(oos);
            if (mols instanceof Closeable) {
                try {
                    ((Closeable) mols).close();
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, "Failed to close iterator", ioe);
                }
            }
        }
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream is) throws IOException {
        return unmarshal(is);
    }

    /**
     * Creates a CloseableMoleculeObjectQueue and writes the deserialized MoleculeObjects
     * to this. Note the queue is returned immediately as soon as deserialization is 
     * started.
     * 
     * @param is
     * @return
     * @throws IOException 
     */
    public Object unmarshal(InputStream is) throws IOException {

        final ObjectInputStream ois = new ObjectInputStream(is);

        final CloseableQueue<MoleculeObject> q = new CloseableMoleculeObjectQueue(50);
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    MoleculeObject mo = (MoleculeObject) ois.readObject();
                    q.add(mo);
                    unmarshalCount++;
                } catch (EOFException e) {
                    break;
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException("Error during deserialization of MoleculeObjects", ex);
                }
            }

            IOHelper.close(is);
            q.close();
        });
        t.start();

        return q;
    }

}
