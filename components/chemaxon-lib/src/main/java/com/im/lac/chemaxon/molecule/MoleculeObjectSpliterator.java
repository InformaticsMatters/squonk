package com.im.lac.chemaxon.molecule;

import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MProp;
import chemaxon.struc.MPropertyContainer;
import com.im.lac.stream.FixedBatchSpliteratorBase;
import com.im.lac.types.MoleculeObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Creates a {@link java.util.Spliterator} of
 * {@link com.im.lac.types.MoleculeObjects} from an {@link java.io.InputStream}
 * (e.g. SD file, or file of smiles strings). This can then be used to create a 
 * Stream of MoleculeObjects. To do so you can use the {@link #asStream(boolean)} 
 * method which will generate a Stream that automatically closes the InputStream 
 * when the Stream is closed.
 * If you create your own stream instead of using the asStream() method make sure 
 * that the InputStream gets closed (hint: use the {@link BaseStream#onClose()} method).
 *
 * The Spliterator has a fixed batch size (the size of any Spliterator returned
 * by the {@link java.util.Spliterator#trySplit()} method) that can be specified
 * in the constructor.
 *
 * @author timbo
 */
public class MoleculeObjectSpliterator extends FixedBatchSpliteratorBase<MoleculeObject> implements AutoCloseable {

    private final MRecordIterator iter;
    private boolean closed = false;

    /**
     * Create spliterator with the specified batch size
     *
     * @param is The input containing molecules
     * @param batchSize Batch size
     * @throws IOException
     */
    public MoleculeObjectSpliterator(InputStream is, int batchSize) throws IOException {
        super(Spliterator.ORDERED | Spliterator.NONNULL, batchSize, Long.MAX_VALUE);
        this.iter = new MRecordIterator(is);
    }

    /**
     * Create spliterator with default batch size of 32
     *
     * @param is
     * @throws IOException
     */
    public MoleculeObjectSpliterator(InputStream is) throws IOException {
        this(is, 32);
    }

    /**
     * Generates a Stream from this Spliterator. When the Stream is closed the
     * underlying InputStream will automatically get closed.
     *
     * @param parallel Should the returned stream be a parallel stream
     * @return
     */
    public Stream<MoleculeObject> asStream(boolean parallel) {
        Stream<MoleculeObject> stream = StreamSupport.stream(this, parallel);
        return stream.onClose(() -> {
            try {
                this.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    MoleculeObject next() {
        //long t0 = System.nanoTime();
        MRecord rec = iter.next();
        String mol = rec.getString();
        String format = rec.getInputFormat();
        if (format == null) {
            format = iter.getRecordReader().getRecognizedFormat();
        }
        if (format != null) {
            // this is a BIG HACK because Marvin does not always give you the right answer
            // TODO - work out something better
            if (format.startsWith("smiles")) {
                format = "smiles";
            } else if (format.startsWith("smarts")) {
                format = "smarts";
            } else if (format.startsWith("mol")) {
                format = "mol";
            } else if (format.startsWith("sdf")) {
                format = "mol"; // yes, this is supposed to be mol
            } else if (format.startsWith("inchi")) {
                format = "inchi";
            } else if (format.startsWith("mrv")) {
                format = "mrv";
            } else {
                format = null;
            }
        }
        String name = rec.getMoleculeName();
        MoleculeObject mo = new MoleculeObject(mol, format);
        if (name != null && name.length() > 0) {
            mo.putValue("name", name);
        }
        MPropertyContainer pc = rec.getPropertyContainer();
        String[] keys = pc.getKeys();
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            MProp prop = pc.get(key);
            mo.putValue(key, prop.getPropValue());
        }
        //long t1 = System.nanoTime();
        //System.out.println("Reading took: " + (t1-t0));
        return mo;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        iter.close();
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public boolean tryAdvance(Consumer<? super MoleculeObject> action) {
        if (iter.hasNext()) {
            action.accept(next());
            return true;
        }
        return false;
    }

}
