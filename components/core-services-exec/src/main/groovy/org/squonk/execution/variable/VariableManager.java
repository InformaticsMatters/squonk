package org.squonk.execution.variable;

import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import org.squonk.notebook.api.VariableKey;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class VariableManager {

    public static Logger LOG = Logger.getLogger(VariableManager.class.getName());

    private final Map<VariableKey, Object> tmpValues = new HashMap<>();

    private final VariableLoader loader;

    public VariableManager(VariableLoader loader) {
        this.loader = loader;
    }

    public void save() throws IOException {
        tmpValues.clear();
        loader.save();
    }

    public <V> void putValue(VariableKey var, Class<V> type, V value, PersistenceType persistenceType) throws IOException {

        switch (persistenceType) {
            case TEXT:
                loader.writeToText(var, value);
                break;
            case JSON:
                loader.writeToJson(var, value);
                break;
            case BYTES:
                loader.writeToBytes(var, "", (InputStream) value);
                break;
            case DATASET:
                Dataset ds = (Dataset) value;
                Dataset.DatasetMetadataGenerator generator = ds.createDatasetMetadataGenerator();

                try (Stream s = generator.getAsStream()) {
                    InputStream is = generator.getAsInputStream(s, true);
                    loader.writeToBytes(var, "#DATA", is);
                } // stream now closed
                DatasetMetadata md = generator.getDatasetMetadata();
                loader.writeToJson(var, md);
                LOG.info("Wrote dataset containing " + md.getSize() + " values");
                break;
            case NONE:
                tmpValues.put(var, value);
                break;
            default:
                throw new IllegalStateException("Type " + persistenceType + " not supported");
        }
    }


    public <V> V getValue(VariableKey key, Class<V> type, PersistenceType persistenceType) throws IOException {

        if (tmpValues.containsKey(key)) {
            return (V) tmpValues.get(key);
        }

        switch (persistenceType) {
            case TEXT:
                return loader.readFromText(key, type);
            case JSON:
                return loader.readFromJson(key, type);
            case BYTES:
                return (V) loader.readBytes(key, "");
            case DATASET:
                DatasetMetadata meta = loader.readFromJson(key, DatasetMetadata.class);
                if (meta == null) {
                    return null;
                }
                InputStream is = loader.readBytes(key, "#DATA");
                if (is == null) {
                    return null;
                }
                return (V) JsonHandler.getInstance().unmarshalDataset(meta, IOUtils.getGunzippedInputStream(is));
            case NONE:
                return (V) tmpValues.get(key);
            default:
                throw new IllegalStateException("Type " + persistenceType + " not supported");
        }
    }

}
