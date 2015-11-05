package com.im.lac.services.job.variable;

import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.types.io.JsonHandler;
import com.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class VariableManager {

    private final Map<Variable, Object> tmpValues = new HashMap<>();
    private final Map<String, Variable> variables = new LinkedHashMap<>();

    private final MemoryVariableLoader loader;

    public VariableManager(MemoryVariableLoader loader) {
        this.loader = loader;
    }

    public void save() throws IOException {
        Iterator<Variable> iter = variables.values().iterator();
        while (iter.hasNext()) {
            Variable var = iter.next();
            if (var.getPersistenceType() == Variable.PersistenceType.NONE) {
                iter.remove();
            }
        }
        loader.save();
    }

    public <V> Variable<V> createVariable(String name, Class<V> type, V value, Variable.PersistenceType persistenceType) throws IOException {

        if (variables.containsKey(name)) {
            throw new IllegalStateException("Variable named " + name + " already exists");
        }

        Variable<V> var = new Variable(name, type, persistenceType);
        switch (var.getPersistenceType()) {
            case TEXT:
                loader.writeToText(name, value);
                break;
            case JSON:
                loader.writeToJson(name, value);
                break;
            case BYTES:
                loader.writeToBytes(name, (InputStream) value);
                break;
            case DATASET:
                Dataset ds = (Dataset) value;
                Dataset.DatasetMetadataGenerator generator = ds.createDatasetMetadataGenerator();

                try (Stream s = generator.getAsStream()) {
                    InputStream is = generator.getAsInputStream(s, true);
                    loader.writeToBytes(name + "#DATA", is);
                } // stream now closed
                DatasetMetadata md = (DatasetMetadata)generator.getDatasetMetadata();
                loader.writeToJson(name + "#META", md);
                break;
            case NONE:
                tmpValues.put(var, value);
            default:
            // do nothing
        }
        variables.put(name, var);
        return var;
    }

    public Variable lookupVariable(String name) {
        return variables.get(name);
    }

    public Set<Variable> getVariables() {
        return new HashSet(variables.values());
    }

    public <V> V getValue(Variable<V> var) throws IOException {
        switch (var.getPersistenceType()) {
            case TEXT:
                return loader.readFromText(var.getName(), var.getType());
            case JSON:
                return loader.readFromJson(var.getName(), var.getType());
            case BYTES:
                return (V) loader.readFromBytes(var.getName());
            case DATASET:
                DatasetMetadata meta = loader.readFromJson(var.getName() + "#META", DatasetMetadata.class);
                InputStream is = loader.readFromBytes(var.getName() + "#DATA");
                return (V) JsonHandler.getInstance().unmarshalDataset(meta, IOUtils.getGunzippedInputStream(is));
            case NONE:
                return (V) tmpValues.get(var);
            default:
                throw new IllegalStateException("Type " + var.getPersistenceType() + " not supported");
        }
    }

}
