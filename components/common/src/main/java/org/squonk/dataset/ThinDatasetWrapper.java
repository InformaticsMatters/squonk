package org.squonk.dataset;


import org.squonk.types.BasicObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * Created by timbo on 19/01/17.
 */
public class ThinDatasetWrapper<T extends BasicObject> {

    private static final Logger LOG = Logger.getLogger(ThinDatasetWrapper.class.getName());

    private final boolean filtering;
    private final boolean preserve;
    private final Class<T> type;


    private Cache<T> cache;
    private DatasetMetadata<T> requestMetadata;

    public ThinDatasetWrapper(Class<T> type, boolean filtering, boolean preserve) {
        this.type = type;
        this.filtering = filtering;
        this.preserve = preserve;
    }

    /** Does the service filter the input (true) or provide all the input records as output (and in the same order).
     *
     * @return
     */
    public boolean isFiltering() {
        return filtering;
    }


    /** Is the main element (e.g. structure for a MoleculeObject) preserved during execution (true) or is it modified and
     * needs to incorporated into results replacing the original.
     *
     * @return
     */
    boolean isPreserve() {
        return preserve;
    }

//    /** Get the fields (values) that will be sent to the service. Keys are the name of the field in the input, value is the
//     * name that the service expects.
//     *
//     * @return
//     */
//    Map<String,String> getFieldMappings();



    /** Create the thin input whihc basically involves stripping out all values and leaving the "bare" object e.g. the
     * MoleculeObject with its UUID and structure, but no values.
     *
     * @param inputDataset
     * @return
     * @throws IOException
     */
    public Dataset<T> prepareInput(Dataset<T> inputDataset) throws IOException {

        if (type != inputDataset.getType()) {
            throw new IllegalStateException("Input dataset is not of type " + type.getName());
        }

        requestMetadata = inputDataset.getMetadata();

        cache = filtering ? new QueueCache<>() : new MapCache<>();
        Stream<T> stream = inputDataset.getStream().sequential()
                .map(fat -> {
                    cache.put(fat);
                    //LOG.info("Fat object:  " + fat);
                    T thin = (T)fat.clone();
                    thin.clearValues();
                    // TODO - allow to incorporate specified values
                    //LOG.info("Thin object: " + thin);
                    return thin;
                });

        DatasetMetadata<T> newMetadata = new DatasetMetadata<>(type);
        newMetadata.setSize(requestMetadata.getSize());

        return new Dataset<T>(type, stream, newMetadata);
    }

    /** Recombine the results with the original data as needed.
     *
     * @param results
     * @return
     * @throws Exception
     */
    public Dataset<T> generateOutput(Dataset<? extends BasicObject> results) throws Exception {

        // we preserve the detail of the original if we are told to or the result type is not of the required type
        // (e.g. the thin service just returned BasicObjects)
        boolean preserveMode = preserve || !type.isAssignableFrom(results.getType());

        DatasetMetadata mergedMetadata = DatasetUtils.mergeDatasetMetadata(requestMetadata, results.getMetadata());

        // merge the results back into the original data
        Stream<T> resultObjs = results.getStream().sequential().map(result -> {
            //LOG.info("Handling: " + m);
            UUID uuid = result.getUUID();
            T o = cache.get(uuid);
            if (o == null) {
                LOG.warning("Object UUID " + uuid + " not found in cache. Strange!");
                return null;
            } else {
                //LOG.fine("Found Mol " + uuid);
                T neu;
                if (preserveMode) {
                    // keep all the original details, but add the new values
                    o.getValues().putAll(result.getValues());
                    neu = o;
                } else {
                    // this cast is safe as we already know the results are of the type we expect
                    neu = (T)result;
                    // use the returned object and its values but also the keep the original values if not present in the result
                    Map<String,Object> resultValues = new LinkedHashMap<>(neu.getValues());
                    neu.clearValues();
                    neu.putValues(o.getValues());
                    neu.putValues(resultValues);
                }
                return neu;
            }
        }).filter(m -> m != null);

        Dataset<T> resultDataset = new Dataset<>(type, resultObjs, mergedMetadata);

        return resultDataset;
    }


    interface Cache<T extends BasicObject> {
        T get(UUID uuid);
        void put(T bo);
    }

    class QueueCache<T extends BasicObject> implements Cache<T> {

        private final Queue<T> queue = new ConcurrentLinkedQueue<>();

        @Override
        public T get(UUID uuid) {
            T mo;
            while ((mo = queue.poll()) != null) {
                if (uuid.equals(mo.getUUID())) {
                    return mo;
                }
            }
            return null;
        }

        @Override
        public void put(T bo) {
            queue.offer(bo);
        }
    }

    class MapCache<T extends BasicObject> implements Cache<T> {

        private final Map<UUID,T> map = new ConcurrentHashMap<>();

        @Override
        public T get(UUID uuid) {
            return map.remove(uuid);
        }

        @Override
        public void put(T bo) {
            map.put(bo.getUUID(), bo);
        }
    }

}
