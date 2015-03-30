package com.im.lac.camel.dataformat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.im.lac.camel.processor.MoleculeObjectSourcer;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.IOHelper;

/**
 * DataFormat that handles marshaling/unmarshaling of MoleculeObjects to/from
 * JSON.
 *
 * @author timbo
 */
public class MoleculeObjectJsonConverter implements DataFormat {

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
    
    public void marshal(Stream<MoleculeObject> mols, OutputStream outputStream) throws IOException {
        marshal(mols.iterator(), outputStream);
    }

    /**
     * Takes an Iterator of MoleculeObjects and writes it to the OutputStream.
     * The Input and OutputStream are closed once processing is complete if the
     * autoCloseAfterMarshal field is set to true, otherwise both must be closed
     * by the caller.
     *
     * @param mols
     * @param outputStream
     * @throws IOException
     */
    public void marshal(Iterator<MoleculeObject> mols, OutputStream outputStream) throws IOException {

        JsonGenerator generator = factory.createGenerator(outputStream);
        generator.writeStartArray();
        while (mols.hasNext()) {
            Object mo = mols.next();
            generator.writeObject((MoleculeObject)mo);
            marshalCount++;
        }

        generator.writeEndArray();
        generator.flush();

        if (autoCloseAfterMarshal) {
            IOHelper.close(outputStream);
            IOUtils.closeIfCloseable(mols);
        }
    }
    
    @Override
    public void marshal(Exchange exchange, Object o, OutputStream out) throws Exception {
        Iterator<MoleculeObject> mols = MoleculeObjectSourcer.bodyAsMoleculeObjectIterator(exchange);
        if (mols == null) {
            throw new IllegalStateException("Can't find MoleculeObjects from Exchange body");
        }
        marshal(mols, out);
    }

    /**
     * Generate an Stream of MoleculeObjects from the JSON input. NOTE: to ensure 
     * the InputStream is closed you should either close the returned stream or
     * close the InputStream once processing is finished.
     *
     * @param stream
     * @return An Stream of MoleculeObjects 
     * @throws IOException
     */
    public Stream<MoleculeObject> unmarshal(InputStream stream) throws IOException {
        JsonSpliterator s = new JsonSpliterator(stream);
        return s.asStream();
    }

    

    @Override
    public Object unmarshal(Exchange exchng, InputStream in) throws Exception {
        // TODO - look at this. Should be StreamProvider? Should handle onClose()?
        Stream<MoleculeObject> stream = unmarshal(in);
        return stream;
    }

    class JsonSpliterator extends Spliterators.AbstractSpliterator<MoleculeObject> implements Closeable {

        List<MoleculeObject> next;
        JsonParser jp;
        InputStream input;
        boolean finished = false;

        JsonSpliterator(InputStream input) throws IOException {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
            this.input = input;
            this.jp = factory.createParser(input);
            JsonToken current = jp.nextToken();
            if (current != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Error: root should be array: quiting.");
            }
        }

        Stream asStream() {

            Stream<MoleculeObject> stream = StreamSupport.stream(this, false);
            return stream.onClose(() -> {
                try {
                    this.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        @Override
        public void close() throws IOException {
            LOG.finer("Closing stream " + input);
            input.close();
            jp.close();
        }

        @Override
        public boolean tryAdvance(Consumer<? super MoleculeObject> action
        ) {
            if (hasNext()) {
                action.accept(next());
                return true;
            }
            return false;
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

        private boolean hasNext() {
            if (finished == true) {
                return false;
            } else if (next != null) {
                return true;
            } else {
                return readNext();
            }
        }

        private MoleculeObject next() {
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

    }
}
