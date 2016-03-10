package org.squonk.types.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.im.lac.dataset.Metadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.im.lac.dataset.JsonMetadataPair;
import com.im.lac.types.BasicObject;
import com.im.lac.types.MoleculeObject;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.util.IOUtils;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author timbo
 */
public class JsonHandler {

    private static final Logger LOG = Logger.getLogger(JsonHandler.class.getName());

    public static final String ATTR_DATASET_METADATA = "DatasetMetadata";
    public static final String ATTR_METADATA = "metadata";
    public static final String ATTR_VALUE_MAPPINGS = "ValueMappings";

    private final ObjectMapper mapper;

    private static final JsonHandler instance = new JsonHandler();

    public JsonHandler() {

        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        // TODO - better way to register custom deserializers ?
        module.addDeserializer(MoleculeObject.class, new MoleculeObjectJsonDeserializer());
        module.addSerializer(MoleculeObject.class, new MoleculeObjectJsonSerializer());
        module.addDeserializer(BasicObject.class, new BasicObjectJsonDeserializer());
        module.addSerializer(BasicObject.class, new BasicObjectJsonSerializer());
        mapper.registerModule(module);
    }

    public static JsonHandler getInstance() {
        return instance;
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    public Object unmarshalItem(Metadata meta, InputStream in) throws ClassNotFoundException, IOException {
        Class cls = Class.forName(meta.getClassName());
        return doUnmarshalItem(cls, meta, in);
    }

    private <T> T doUnmarshalItem(Class<T> cls, Metadata meta, InputStream in) throws IOException {
        ContextAttributes attrs = ContextAttributes.getEmpty()
                .withSharedAttribute(ATTR_METADATA, meta);
        ObjectReader reader = mapper.readerFor(cls).with(attrs);
        T result = reader.readValue(in);
        return result;
    }

    public Iterator<? extends Object> unmarshalItemsAsIterator(Metadata meta, InputStream in) throws IOException, ClassNotFoundException {
        Class cls = Class.forName(meta.getClassName());
        return doUnmarshalItemsAsIterator(cls, meta, in);
    }

    public Stream<? extends Object> unmarshalItemsAsStream(Metadata meta, InputStream in) throws IOException, ClassNotFoundException {
        Iterator<? extends Object> iter = unmarshalItemsAsIterator(meta, in);
        int size = meta.getSize();
        Spliterator<? extends Object> spliterator;
        if (size == 0) {
            spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        } else {
            spliterator = Spliterators.spliterator(iter, size, Spliterator.NONNULL | Spliterator.ORDERED);
        }
        return StreamSupport.stream(spliterator, true);
    }

    private <T> Iterator<T> doUnmarshalItemsAsIterator(Class<T> cls, Metadata meta, InputStream in) throws IOException {
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectReader reader = mapper.readerFor(cls).with(attrs);
        Iterator<T> result = reader.readValues(in);
        return result;
    }

    public void marshalItem(Object item, Metadata meta, OutputStream outputStream) throws IOException {
        LOG.fine("marshalling item to JSON");
        meta.setClassName(item.getClass().getName());
        meta.setType(Metadata.Type.ITEM);
        meta.setSize(1);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectWriter writer = mapper.writerFor(item.getClass()).with(attrs);
        writer.writeValue(outputStream, item);
    }

    public String marshalItemAsString(Object item, Metadata meta) throws IOException {
        LOG.fine("marshalling item to JSON");
        meta.setClassName(item.getClass().getName());
        meta.setType(Metadata.Type.ITEM);
        meta.setSize(1);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectWriter writer = mapper.writerFor(item.getClass()).with(attrs);
        return writer.writeValueAsString(item);
    }

    public byte[] marshalItemAsBytes(Object item, Metadata meta) throws IOException {
        LOG.fine("marshalling item to JSON");
        meta.setClassName(item.getClass().getName());
        meta.setType(Metadata.Type.ITEM);
        meta.setSize(1);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute(ATTR_METADATA, meta);
        ObjectWriter writer = mapper.writerFor(item.getClass()).with(attrs);
        return writer.writeValueAsBytes(item);
    }

    public void marshalItems(Stream items, Metadata meta, OutputStream outputStream) throws IOException {
        LOG.fine("marshalling items to JSON");
        meta.setType(Metadata.Type.STREAM);
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("metadata", meta);
        ObjectWriter ow = mapper.writer().with(attrs);
        try (SequenceWriter sw = ow.writeValuesAsArray(outputStream)) {

            final AtomicReference<Class> classNameRef = new AtomicReference<>();
            long count = ((Stream) items.sequential()).peek((i) -> {
                try {
                    if (classNameRef.get() == null) {
                        LOG.log(Level.FINE, "Setting type to {0}", i.getClass());
                        classNameRef.set(i.getClass());
                    } else if (classNameRef.get() != i.getClass()) {
                        throw new IllegalStateException("Items must all be of the same type. Previous: " + classNameRef.get().getName() + " Current: " + i.getClass().getName());
                    }
                    sw.write(i);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write object: " + i, ex);
                }
            }).count();
            meta.setSize(count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count);
            if (count > 0) {
                meta.setClassName(classNameRef.get().getName());
            }

        } finally {
            outputStream.close();
        }
    }

    public String objectToJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public void objectToFile(Object o, File f) throws JsonProcessingException, IOException {
        mapper.writeValue(f, o);
    }

    public <T> T objectFromJson(InputStream is, Class<T> type) throws IOException {
        return mapper.readValue(is, type);
    }

    public <T> T objectFromJson(InputStream is, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.readerFor(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValue(is);
    }

    public <T> T objectFromJson(String s, Class<T> type) throws IOException {
        return mapper.readValue(s, type);
    }

    public <T> T objectFromJson(String s, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.readerFor(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValue(s);
    }

    public <T> Iterator<T> iteratorFromJson(String s, Class<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        return reader.readValues(s);
    }

    public <T> Iterator<T> iteratorFromJson(String s, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.readerFor(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValues(s);
    }

    public <T> Iterator<T> iteratorFromJson(InputStream is, Class<T> type, Metadata meta) throws IOException {
        ObjectReader reader = mapper.readerFor(type).withAttribute(ATTR_METADATA, meta);
        return reader.readValues(is);
    }

    public <T> Iterator<T> iteratorFromJson(InputStream is, Class<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        return reader.readValues(is);
    }

    public <T> Stream<T> streamFromJson(final String json, final Class<T> type) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        Iterator<T> iter = reader.readValues(json);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, true);
    }

    public <T> Stream<T> streamFromJson(final InputStream is, final Class<T> type, final boolean autoClose) throws IOException {
        return streamFromJson(is, type, (Map) null, autoClose);
    }

    /**
     * Generate a Stream of objects of the specified type. The input stream must
     * contain JSON containing objects of just that type.
     *
     * @param <T>
     * @param is
     * @param type
     * @param mappings
     * @param autoClose
     * @return
     * @throws IOException
     */
    public <T> Stream<T> streamFromJson(final InputStream is, final Class<T> type, Map<String, Class> mappings, final boolean autoClose) throws IOException {
        ObjectReader reader = mapper.readerFor(type);
        if (mappings != null) {
            reader = reader.withAttribute(ATTR_VALUE_MAPPINGS, mappings);
        }
        Iterator<T> iter = reader.readValues(is);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
        Stream<T> stream = StreamSupport.stream(spliterator, true);
        if (autoClose) {
            return stream.onClose(() -> IOUtils.close(is));
        } else {
            return stream;
        }
    }

    /**
     * Takes the object(s) and generates JSON and corresponding metadata.
     *
     * @param item The Object, Stream or List to marshal to JSON.
     * @param gzip Whether to gzip the stream. Usually this inputStream best as
     * it reduces IO.
     * @return the marshal results, with the metadata complete once the
     * InputStream has been fully read. You are responsible for closing the
     * InputStream when complete.
     * @throws IOException
     */
    public JsonMetadataPair generateJsonForItem(Object item, boolean gzip) throws IOException {
        final PipedInputStream pis = new PipedInputStream();
        final OutputStream out = new PipedOutputStream(pis);
        final Metadata meta = new Metadata();

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable c = (Callable) () -> {
            if (item instanceof Stream) {
                marshalItems((Stream) item, meta, gzip ? new GZIPOutputStream(out) : out);
            } else if (item instanceof List) {
                marshalItems(((List) item).stream(), meta, gzip ? new GZIPOutputStream(out) : out);
            } else {
                marshalItem(item, meta, gzip ? new GZIPOutputStream(out) : out);
            }
            return true;
        };
        executor.submit(c);
        executor.shutdown();
        return new JsonMetadataPair(pis, meta);

    }

    public <T> InputStream marshalStreamToJsonArray(Stream<T> stream, boolean gzip) throws IOException {
        final PipedInputStream pis = new PipedInputStream();
        final OutputStream pout = new PipedOutputStream(pis);
        final OutputStream out = (gzip ? new GZIPOutputStream(pout) : pout);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable c = (Callable) () -> {
            marshalStreamToJsonArray(stream, out);
            return true;
        };
        executor.submit(c);
        executor.shutdown();
        return pis;
    }

    public <T> void marshalStreamToJsonArray(Stream<T> stream, OutputStream out) throws IOException {

        ObjectWriter ow = mapper.writer();
        try (SequenceWriter sw = ow.writeValuesAsArray(out)) {
            stream.forEachOrdered((i) -> {
                //LOG.info("Writing to json: "  + i);
                try {
                    sw.write(i);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write object: " + i, ex);
                }
            });
        } finally {
            out.close();
            stream.close();
        }
    }

    /**
     * Use the metadata to deserialize the JSON in the InputStream to a Dataset
     * of the right type.
     *
     * @param <T> The type of objects in the Dataset
     * @param metadata The metadata describing the Dataset
     * @param json The JSON
     * @return
     * @throws IOException
     */
    public <T extends BasicObject> Dataset<T> unmarshalDataset(DatasetMetadata<T> metadata, InputStream json) throws IOException {
        ObjectReader reader = mapper.readerFor(metadata.getType()).with(ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_DATASET_METADATA, metadata));
        MappingIterator iter = reader.readValues(json);
        return new Dataset<>(metadata.getType(), iter, metadata);
    }

    public <T extends BasicObject> Dataset<T> unmarshalDataset(DatasetMetadata<T> metadata, String json) throws IOException {
        ObjectReader reader = mapper.readerFor(metadata.getType()).with(ContextAttributes.getEmpty().withSharedAttribute(JsonHandler.ATTR_DATASET_METADATA, metadata));
        MappingIterator iter = reader.readValues(json);
        return new Dataset<>(metadata.getType(), iter, metadata);
    }

}
