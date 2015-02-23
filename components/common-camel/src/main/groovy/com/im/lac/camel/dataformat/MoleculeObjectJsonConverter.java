package com.im.lac.camel.dataformat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.IOUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import org.apache.camel.util.IOHelper;

/**
 * DataFormat that handles marshaling/unmarshaling of MoleculeObjects to/from JSON.
 * @author timbo
 */
public class MoleculeObjectJsonConverter {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectJsonConverter.class.getName());

    /**
     * Should the input and output when marshaling be closed when processing is
     * complete. Default is true
     */
    private boolean autoCloseAfterMarshal = true;
    private final JsonFactory factory;
    
    private int marshalCount = 0;

    public int getMarshalCount() {
        return marshalCount;
    }

    private int unmarshalCount = 0;

    public int getUnmarshalCount() {
        return unmarshalCount;
    }
    
    public MoleculeObjectJsonConverter() {
        this(true);
    }

    public MoleculeObjectJsonConverter(boolean autoCloseAfterMarshal) {
        this.autoCloseAfterMarshal = autoCloseAfterMarshal;
        this.factory = new MappingJsonFactory();
    }

    /**
     * Takes an Iterator of MoleculeObjects and writes
     * it to the OutputStream. The Input and OutputStream are closed once
     * processing is complete if the autoCloseAfterMarshal field is set to true,
     * otherwise both must be closed by the caller.
     *
     * @param mols
     * @param stream
     * @throws IOException
     */
    public void marshal(Iterator<MoleculeObject> mols, OutputStream stream) throws IOException {

        JsonGenerator generator = factory.createGenerator(stream);
        generator.writeStartArray();
        while (mols.hasNext()) {
            generator.writeObject(mols.next());
            marshalCount++;
        }

        generator.writeEndArray();
        generator.flush();
        
        if (autoCloseAfterMarshal) {
            IOHelper.close(stream);
            IOUtils.closeIfCloseable(mols);
        }
    }

    /**
     * Generate an Iterator of MoleculeObjects from the JSON stream. NOTE: as
     * we can't tell when the processing is finished the Iterator that is
     * returned implements Closeable so that it can be closed by the caller when
     * finished to ensure the underlying stream is closed. Alternatively the
     * caller can close the InputStream directly.
     *
     * @param stream
     * @return An Iterable of MoleculeObjects that also implements java.ioCloseable
     * @throws IOException
     */
    public MoleculeObjectIterable unmarshal(InputStream stream) throws IOException {
        return new JsonIterator(stream);
    }

    class JsonIterator implements MoleculeObjectIterable, Iterator<MoleculeObject>, Closeable {

        List<MoleculeObject> next;
        JsonParser jp;
        InputStream stream;
        boolean finished = false;

        JsonIterator(InputStream stream) throws IOException {
            this.stream = stream;
            this.jp = factory.createParser(stream);
            JsonToken current = jp.nextToken();
            if (current != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Error: root should be array: quiting.");
            }
        }

        private boolean readNext() {
            try {
                if (jp.nextToken() == JsonToken.END_ARRAY) {
                    next = null;
                    finished = true;
                    return false;
                } else {
                    MoleculeObject result = jp.readValueAs(MoleculeObject.class);
                    //System.out.println("Read: " + result);
                    next = Collections.singletonList(result);
                    
                }
                return true;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read", ex);
            }
        }

        @Override
        public boolean hasNext() {
            if (finished == true) {
                return false;
            } else if (next != null) {
                return true;
            } else {
                return readNext();
            }
        }

        @Override
        public MoleculeObject next() {
            if (next == null) {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more data");
                }
            }
            MoleculeObject result = next.get(0);
            next = null;
            unmarshalCount++;
            return result;
        }

        @Override
        public void close() throws IOException {
            LOG.finer("Closing stream " + stream);
            stream.close();
            jp.close();
        }

        @Override
        public Iterator<MoleculeObject> iterator() {
            return this;
        }

    }
}
