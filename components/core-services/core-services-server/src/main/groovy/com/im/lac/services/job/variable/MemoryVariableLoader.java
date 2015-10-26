package com.im.lac.services.job.variable;

import com.squonk.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author timbo
 */
public class MemoryVariableLoader implements VariableLoader {

    Map<Variable, Object> values = new HashMap<>();

    /**
     * Removes non persistent values.
     */
    @Override
    public void save() {
        Iterator<Map.Entry<Variable, Object>> it = values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Variable, Object> e = it.next();
            if (!e.getKey().isPersistent()) {
                it.remove();
            }
        }
    }

    @Override
    public <V> V loadVariable(Variable<V> var) {
        return (V) values.get(var);
    }

    @Override
    public <V> void saveVariable(Variable<V> var, V value) {
        values.put(var, value);
    }

    @Override
    public <V> void writeVariable(Variable<V> var, InputStream is) throws IOException {
        byte[] bytes = IOUtils.convertStreamToBytes(is, 1000);
        values.put(var, bytes);
    }
}
