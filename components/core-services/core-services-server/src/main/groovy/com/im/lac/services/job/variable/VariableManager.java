package com.im.lac.services.job.variable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author timbo
 */
public class VariableManager {

    private final List<Variable> variables = new ArrayList<>();

    private final VariableLoader loader;

    public VariableManager(VariableLoader loader) {
        this.loader = loader;
    }

    public void save() throws IOException {
        loader.save();
    }

    public <V> Variable<V> createVariable(String name, Class<V> type, V value, boolean persistent) throws IOException {

        Variable<V> v = new Variable(name, type, persistent);
        loader.saveVariable(v, value);
        variables.add(v);
        return v;
    }

    public <V> Variable<V> writeVariable(String name, Class<V> type, InputStream is, boolean persistent) throws IOException {
        Variable<V> v = new Variable(name, type, persistent);
        loader.writeVariable(v, is);
        variables.add(v);
        return v;
    }

    public Variable lookupVariable(String name) {
        for (Variable v : variables) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public <V> V getValue(Variable<V> var) throws IOException {
        return loader.loadVariable(var);
    }

}
