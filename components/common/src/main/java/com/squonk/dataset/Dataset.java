package com.squonk.dataset;

import com.im.lac.types.BasicObject;
import com.im.lac.util.StreamProvider;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Core dataset class for a set of objects of the same type, along with
 * associated metadata. This is designed to handle very large datasets by
 * allowing streaming of data. The use of this class is quite complex. Make sure
 * you read the JavaDocs thoroughly.
 *
 * <h3>Content definition</h3>
 * <p>
 * The objects can be defined as one of
 * <ul>
 * <li>InputStream containing JSON (in which case metadata MUST be specified to
 * describe how to deserialize)</li>
 * <li>URL from which the data can be read. Similar to InputStream except that
 * the stream can be read multiple times</li>
 * <li>Iterator</li>
 * <li>Stream</li>
 * <li>List</li>
 * </ul>
 * Of these ONLY List and URL can be read more than once, so, for instance, if
 * you ask for a Stream twice then the second time will cause an error unless
 * the underlying source is a List or URL. To make this easier there are ways to
 * automatically "materialize" the underlying as a List (e.g. convert a
 * InputStream/Interator/Stream into a List. For instance if the underlying data
 * is specified as InputStream/Interator/Stream and you call {@link getItems()}
 * then the underlying source will be converted to a List so that it can be read
 * repeatedly. HOWEVER do this with care, as if the dataset is large you may
 * exhaust memory. Usually it is better to stream the data using a Stream or
 * Iterator as long as you only need to read once.
 *
 * <h3>Metadata</h3>
 * The key function of the metadata is to describe what type of object is
 * present in the underlying InputStream/Interator/Stream/List (must be
 * homogeneous) and what type of properties (values) they contain. When the
 * source is a List/Iterator/Stream then the Metadata is optional, as the
 * core information can be extracted from the underlying objects but when the
 * source is InputStream or URL the Metadata is mandatory so that the JSON can
 * be deserialized.
 *
 * <h3>Serialization/deserialization to JSON</h3>
 * As the underlying contents are potentially very large we rely on streaming
 * the contents to/from JSON. This means that the contents are stored separate
 * from the metadata. To serialize an Dataset you must first serialize the items
 * (as an array of JSON objects) - use the
 * {@link JsonHandler.serializeStreamAsJsonArray(Stream, boolean)} helper method
 * for this. You must then ALSO save the metadata as its separate JSON 'file'.
 * <br>
 * Similarly when reading JSON you must first read the metadata from its JSON
 * and the use that metadata to deserialize the contents (the JSON array of
 * items). The
 * {@link JsonHandler.deserializeDataset(DatasetMetadata, InputStream)} utility
 * method handles this for you.
 * <p>
 * The key reason for this separation of data and metadata is that in order to
 * read the JSON you first need to have the metadata, but when writing to JSON
 * you might only have the metadata once you have written the contents (as the
 * stream can only be read once).
 * <p>
 * Note: the individual data items (must be subclasses of {@link BasicObject})
 * must also be serializable/deserializable to/from JSON. If you use your own
 * custom types here you are responsible for ensuring that Jackson can handle
 * this.
 *
 *
 * @author Tim Dudgeon &lt;tdudgeon@informaticsmatters.com&gt;
 * @param <T>
 */
public class Dataset<T extends BasicObject> implements DatasetProvider, StreamProvider<T> {

    private static final Logger LOG = Logger.getLogger(Dataset.class.getName());
    private static final String MSG_ALREADY_CONSUMED = "Input not defined or already consumed";

    protected List<T> list;
    protected Stream<T> stream;
    protected Iterator<T> iter;
    protected InputStream inputStream;
    protected URL url;
    private DatasetMetadata metadata;
    private final Class<T> type;

    private final Object lock = new Object();

    /**
     * Items as list. Metadata will need to be generated
     *
     * @param type
     * @param items
     */
    public Dataset(Class<T> type, Collection<T> items) {
        this(type, items, null);
    }

    /**
     * Items as stream. Metadata will need to be generated
     *
     * @param type
     * @param objects
     */
    public Dataset(Class<T> type, Stream<T> objects) {
        this(type, objects, null);
    }

    /**
     * Items as iterator. Metadata will need to be generated
     *
     * @param type
     * @param iter
     */
    public Dataset(Class<T> type, Iterator<T> iter) {
        this(type, iter, null);
    }

    public Dataset(Class<T> type, Collection<T> items, DatasetMetadata<T> metadata) {
        this.type = type;
        this.list = new ArrayList<>();
        list.addAll(items);
        this.metadata = metadata;
    }

    public Dataset(Class<T> type, Stream<T> objects, DatasetMetadata<T> metadata) {
        this.type = type;
        this.stream = objects;
        this.metadata = metadata;
    }

    public Dataset(Class<T> type, Iterator<T> iter, DatasetMetadata<T> metadata) {
        this.type = type;
        this.iter = iter;
        this.metadata = metadata;
    }

    public Dataset(Class<T> type, URL url, DatasetMetadata<T> metadata) {
        this.type = type;
        this.url = url;
        this.metadata = metadata;
    }

    /**
     * Items as InputStream. Metadata must be provided otherwise we can't
     * deserialize the JSON.
     *
     * @param type
     * @param inputStream
     * @param metadata
     */
    public Dataset(Class<T> type, InputStream inputStream, DatasetMetadata<T> metadata) {
        this.type = type;
        this.inputStream = inputStream;
        this.metadata = metadata;
    }

    @Override
    public Dataset getDataset() {
        return this;
    }

    /**
     * Get the metadata. Can be null, in which case it can be generated.
     *
     * @return
     */
    public DatasetMetadata getMetadata() {
        return metadata;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * Get the underlying source type. Note that this can change. For instance
     * if the source is Stream and you ask for it as a List then, as the Stream
     * can only be read once, the Stream is read and converted to a List and the
     * underlying type changes to List.
     *
     * @return
     */
    public Class getSourceType() {
        if (url != null) {
            return url.getClass();
        } else if (list != null) {
            return list.getClass();
        } else if (stream != null) {
            return stream.getClass();
        } else if (iter != null) {
            return iter.getClass();
        } else if (inputStream != null) {
            return inputStream.getClass();
        } else {
            return null;
        }
    }

    /**
     * Get the contents as List, converting the underlying representation to
     * List if necessary. NOTE it is not advised to do this UNLESS you are
     * CERTAIN that the number of elements is relatively small. Using Stream is
     * better for performance and memory utilisation.
     *
     * @return
     * @throws IOException
     */
    public List<T> getItems() throws IOException {
        if (list == null) {
            synchronized (lock) {
                try (Stream<T> st = doGetAsStream()) {
                    List<T> l = st.collect(Collectors.toList());
                    stream = null;
                    iter = null;
                    inputStream = null;
                    list = l;
                }
            }
        }
        return list;
    }

    /**
     * Get the contents as as Stream. NOTE: Unless the underlying representation
     * is List then the contents cannot be obtained a second time.
     *
     * @return
     * @throws IOException
     */
    public Stream<T> getStream() throws IOException {
        synchronized (lock) {
            return doGetAsStream();
        }
    }

    /**
     * Replace the current contents with this stream, normally a stream
     * generated from the existing one.
     *
     * @param stream
     */
    public void replaceStream(Stream<T> stream) {
        synchronized (lock) {
            this.stream = stream;
            this.list = null;
            this.inputStream = null;
            this.iter = null;
        }
    }

    Stream<T> doGetAsStream() throws IOException {
        if (list != null) {
            return list.stream();
        } else if (url != null) {
            Stream<T> st = createStreamFromInputStream(url.openStream(), metadata, true);
            return st;
        } else if (stream != null) {
            Stream<T> s = stream;
            stream = null;
            return s;
        } else if (iter != null) {
            Spliterator split = Spliterators.spliteratorUnknownSize(iter, Spliterator.NONNULL | Spliterator.ORDERED);
            Stream<T> s = StreamSupport.stream(split, true);
            iter = null;
            return s;
        } else if (inputStream != null) {
            Stream<T> st = createStreamFromInputStream(inputStream, metadata, true);
            inputStream = null;
            return st;
        } else {
            throw new IllegalStateException(MSG_ALREADY_CONSUMED);
        }
    }

    /**
     * Get the contents as as Iterator. NOTE: Unless the underlying
     * representation is List or URL then the contents cannot be obtained a second
     * time.
     *
     * @return
     * @throws IOException
     */
    public Iterator<T> asIterator() throws IOException {
        synchronized (lock) {
            return doGetAsIterator();
        }
    }

    Iterator<T> doGetAsIterator() throws IOException {
        if (list != null) {
            return list.iterator();
        } else if (url != null) {
            Stream<T> st = createStreamFromInputStream(url.openStream(), metadata, true);
            return st.iterator();
        } else if (stream != null) {
            Stream<T> s = stream;
            stream = null;
            return s.iterator();
        } else if (iter != null) {
            Iterator<T> i = iter;
            iter = null;
            return i;
        } else if (inputStream != null) {
            Stream<T> st = createStreamFromInputStream(inputStream, metadata, true);
            inputStream = null;
            return st.iterator();
        } else {
            throw new IllegalStateException(MSG_ALREADY_CONSUMED);
        }
    }

    private Stream<T> createStreamFromInputStream(InputStream is, DatasetMetadata metadata, boolean autoclose) throws IOException {
        InputStream gunzipped = IOUtils.getGunzippedInputStream(is);
        return JsonHandler.getInstance().streamFromJson(gunzipped, metadata.getType(), metadata.getValueClassMappings(), autoclose);
    }

    /**
     * Returns the data as an InputStream. If the underlying source was
     * InputStream it is returned. Otherwise it is obtained using as a Stream
     * (see notes for {@link #getStream()} and serialised to JSON. You are
     * responsible for closing the InputStream when finished.
     *
     * @param gzip Whether to gzip the input.
     * @return
     * @throws IOException
     */
    public InputStream getInputStream(boolean gzip) throws IOException {
        synchronized (lock) {
            return doGetAsInputStream(gzip);
        }
    }

    InputStream doGetAsInputStream(boolean gzip) throws IOException {
        if (url != null) {
            if (gzip) {
                return IOUtils.getGzippedInputStream(url.openStream());
            } else {
                return url.openStream();
            }
        } else if (inputStream != null) {
            InputStream is = inputStream;
            inputStream = null;
            if (gzip) {
                return IOUtils.getGzippedInputStream(is);
            } else {
                return is;
            }
        } else {
            return JsonHandler.getInstance().marshalStreamToJsonArray(getStream(), gzip);
        }
    }

    /**
     * Replace the contents the provided Stream, converted to a List. The
     * purpose of this is that the current contents can be transformed by
     * obtaining as a Stream, adding so Stream processing operations to the
     * stream and then saving the contents as a List using this method. The
     * notes about large datasets apply here.
     *
     * @param stream
     * @return
     */
    public List<T> convertToList(Stream<T> stream) {
        List<T> l = stream.collect(Collectors.toList());
        this.inputStream = null;
        this.iter = null;
        this.stream = null;
        this.list = l;
        return l;
    }

    /**
     * Generate metadata for the items. This will require converting to a List
     * so the notes about large datasets apply here.
     *
     * @throws IOException
     */
    public void generateMetadata() throws IOException {
        Class sourceType = getSourceType();
        if (sourceType == null) {
            throw new IllegalStateException(MSG_ALREADY_CONSUMED);
        }

        Dataset.DatasetMetadataGenerator generator = createDatasetMetadataGenerator();
        try (Stream s = generator.getAsStream()) {
            if (List.class.isAssignableFrom(sourceType)) {
                // we have a list so we can re-use it
                // need to consume the stream, so we count it
                s.count();
            } else {
                // can only read once, so need to collect into a list, generating the metadata as we go.
                convertToList(s);
            }
        }
        // finally wait for the MD generation to complete
        generator.getDatasetMetadata();
    }

   /**
    * Create a DatasetMetadataGenerator using the built in Stream. Use this if you don't
    * need to perform any additional operations on the stream.
    * @return
    * @throws IOException 
    */   
    public DatasetMetadataGenerator<T> createDatasetMetadataGenerator() throws IOException {
        return new DatasetMetadataGenerator<>(getStream());
    }

    /**
     * Create a DatasetMetadataGenerator using the provided Stream (see notes above) 
     * and add functions to the Stream to
     * generate the metadata as the stream is consumed. Once Stream.close() is
     * called the metadata will be updated to reflect the contents, but this is done
     * is a different thread so you need to wait for it to complete. 
     * <br>
     * For example:
     * <br>
     * <code>
     * Dataset ds = ...
     * Stream stream = ds.getStream();
     * // add operations to the stream if required. these happend prior to metadata generation
     * DatasetMetadataGenerator generator = ds.createDatasetMetadataGenerator(stream);
     * try (Stream s = generator.getAsStream())) {
     *     // add operations to the stream if required. these happend after to metadata generation
     *     long size = s.count(); // must perform a terminal operation on the stream
     * }
     * DatasetMetadata meta = generator.getDatasetMetadata(); // this can be blocking
     * assert meta != null; // you now have metadata
     * </code>
     *
     * NOTE: the call to {@link DatasetMetadataGenerator#getDatasetMetadata} blocks
     * until he metadata is generated, and this only happens once the Stream is closed.
     * So unless you close the stream it will block forever. Remember to close the stream!
     * Also remember to perform a terminal operation on the stream.
     * 
     * 
     * @param source
     * @return
     */
    public DatasetMetadataGenerator<T> createDatasetMetadataGenerator(Stream<T> source) {
        return new DatasetMetadataGenerator<>(source);
    }

    public class DatasetMetadataGenerator<T extends BasicObject> {

        private final Stream<T> source;
        private final CompletableFuture<DatasetMetadata<T>> future = new CompletableFuture<>();

        DatasetMetadataGenerator(Stream<T> source) {
            this.source = derriveStream(source);
        }

        /**
         * Get the DatasetMetadata, blocking until it is generated. Make sure you perform
         * a terminal operation on the stream AND close it otherwise the metadata will
         * never be generated.
         * @return 
         */
        public DatasetMetadata<T> getDatasetMetadata() {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException("Fetching Metadata failed", ex);
            }
        }

        /**
         * Get the Stream with the metadata generating operations appended.
         * @return 
         */
        public Stream<T> getAsStream() {
            return source;
        }

        /**
         * Get an InputStream of a JSON array of the objects in the Stream. Once
         * the InputStream is fully consumed you must close the Stream to generate
         * the metadata.
         * @param s
         * @param gzip
         * @return
         * @throws IOException 
         */
        public InputStream getAsInputStream(Stream<T> s, boolean gzip) throws IOException {
            return JsonHandler.getInstance().marshalStreamToJsonArray(s, gzip);
        }

        private Stream<T> derriveStream(Stream<T> stream) {
            AtomicInteger count = new AtomicInteger(0);
            AtomicReference<Class> type = new AtomicReference<>();
            Map<String, Class> mappings = Collections.synchronizedMap(new HashMap<>());
            return stream.peek((i) -> {
                count.incrementAndGet();

                // check the type of item - must be homogeneous
                Class myCls = i.getClass();
                Class currCls = type.get();
                if (currCls == null) {
                    type.set(myCls);
                } else if (myCls != currCls) {
                    throw new IllegalStateException("Inconsistent type. Previously found " + currCls + " but now encountered " + myCls);
                }

                // handle the value types
                for (Map.Entry<String, Object> e : i.getValues().entrySet()) {
                    String propname = e.getKey();
                    Object value = e.getValue();
                    if (value != null) {

                        Class old = mappings.get(propname);
                        if (old == null) {
                            mappings.put(propname, value.getClass());
                        } else if (old != value.getClass()) {
                            LOG.log(Level.WARNING,
                                    "Property type inconsistent for {0}. Old: {1} Found: {2}. Old value will be used, but inconsistency could cause problems.",
                                    new Object[]{propname, old.getName(), value.getClass().getName()});
                        }
                    }
                }
            }).onClose(() -> {
                if (metadata == null) {
                    metadata = new DatasetMetadata(type.get(), mappings, count.get());
                } else {
                    metadata.setSize(count.get());
                    metadata.getValueClassMappings().clear();
                    metadata.getValueClassMappings().putAll(mappings);
                }
                future.complete(metadata);
            });
        }
    }
}
