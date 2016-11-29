package org.squonk.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.squonk.util.IOUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Generic model object designed to allow information to be persisted in a fairly generic manner.
 * This assumes that there is one primary object that stores the core information and this object is persisted
 * as JSON along with this object. It is assumed that this item is reasonably compact (e.g. can be easily held
 * in memory and can be stored in a database text column).
 * In addition there can be one or more secondary data items that are persisted as InputStreams, but can be large
 * in size. These items are identified by the streamNames property.
 * When reading values using this class all streams MUST be closed when finished, even if they are not read. The
 * close() method can be used to do this.
 * <p>
 * <p>
 * <p>
 * Created by timbo on 18/10/2016.
 */
public class GenericModel<T> implements AutoCloseable {

    private final T modelItem;
    private final String[] streamNames;
    private final Map<String, InputStream> streams = new HashMap<>();

    public GenericModel(
            @JsonProperty("modelItem") T modelItem,
            @JsonProperty("streamNames") String[] streamNames) {
        this.modelItem = modelItem;
        this.streamNames = streamNames;
    }


    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public T getModelItem() {
        return modelItem;
    }

    public String[] getStreamNames() {
        return streamNames;
    }

    public InputStream getStream(String name) {
        return streams.get(name);
    }

    public void setStream(String name, InputStream stream) {
        streams.put(name, stream);
    }

    public void close() {
        streams.values().forEach(is -> IOUtils.close(is));
    }

    public interface StreamIterator {

        boolean hasNext();
        void next();
        String currentName();
        InputStream currentInputStream();
    }

    public static class DefaultStreamProvider implements StreamIterator {

        private final Iterator<Map.Entry<String, InputStream>> iterator;
        private Map.Entry<String, InputStream> current;

        public DefaultStreamProvider(Map<String, InputStream> streamsMap) {
            this.iterator = streamsMap.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public void next() {
            current = iterator.next();
        }

        @Override
        public String currentName() {
            return current.getKey();
        }

        @Override
        public InputStream currentInputStream() {
            return current.getValue();
        }
    }

}
