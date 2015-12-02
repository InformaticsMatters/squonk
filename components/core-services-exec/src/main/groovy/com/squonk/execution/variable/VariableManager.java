package com.squonk.execution.variable;

import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class VariableManager {

    private final Map<String, Object> tmpValues = new HashMap<>();

    private final VariableLoader loader;

    public VariableManager(VariableLoader loader) {
        this.loader = loader;
    }

    public void save() throws IOException {
        tmpValues.clear();
        loader.save();
    }

    public <V> void putValue(String name, Class<V> type, V value, PersistenceType persistenceType) throws IOException {

        switch (persistenceType) {
            case TEXT:
                loader.writeToText(name, value);
                break;
            case JSON:
                loader.writeToJson(name, value);
                break;
            case BYTES:
                loader.writeToBytes(name, "", (InputStream) value);
                break;
            case DATASET:
                Dataset ds = (Dataset) value;
                Dataset.DatasetMetadataGenerator generator = ds.createDatasetMetadataGenerator();

                try (Stream s = generator.getAsStream()) {
                    InputStream is = generator.getAsInputStream(s, true);
                    loader.writeToBytes(name, "#DATA", is);
                } // stream now closed
                DatasetMetadata md = generator.getDatasetMetadata();
                loader.writeToJson(name, md);
                break;
            case NONE:
                tmpValues.put(name, value);
                break;
            default:
                throw new IllegalStateException("Type " + persistenceType + " not supported");
        }
    }


    public <V> V getValue(String name, Class<V> type, PersistenceType persistenceType) throws IOException {

        if (tmpValues.containsKey(name)) {
            return (V) tmpValues.get(name);
        }

        switch (persistenceType) {
            case TEXT:
                return loader.readFromText(name, type);
            case JSON:
                return loader.readFromJson(name, type);
            case BYTES:
                return (V) loader.readBytes(name, "");
            case DATASET:
                DatasetMetadata meta = loader.readFromJson(name, DatasetMetadata.class);
                if (meta == null) {
                    return null;
                }
                InputStream is = loader.readBytes(name, "#DATA");
                if (is == null) {
                    return null;
                }
                return (V) JsonHandler.getInstance().unmarshalDataset(meta, IOUtils.getGunzippedInputStream(is));
            case NONE:
                return (V) tmpValues.get(name);
            default:
                throw new IllegalStateException("Type " + persistenceType + " not supported");
        }
    }

}
