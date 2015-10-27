package com.im.lac.services.job.variable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author timbo
 */
public class VariableManager {

    //private final List<Variable> variables = new ArrayList<>();
    private final VariableLoader loader;

    public VariableManager(VariableLoader loader) {
        this.loader = loader;
    }

    public void save() throws IOException {
        loader.save();
    }

    public <V> Variable<V> createVariable(String name, Class<V> type, V value, Variable.PersistenceType persistenceType) throws IOException {
        return createVariable(name, type, value, persistenceType, null);
    }

    public <V> Variable<V> createVariable(String name, Class<V> type, V value, Variable.PersistenceType persistenceType, InputStream is) throws IOException {
        Variable<V> v = new Variable(name, type, persistenceType);
        loader.writeVariable(v, value);
        //variables.add(v);
        return v;
    }

    public Variable lookupVariable(String name) {
        Set<Variable> vars = loader.getVariables();
        for (Variable v : vars ) {
            if (v.getName().equals(name)) {
                return v;
            }
        }
        return null;
    }

    public Set<Variable> getVariables() {
        return loader.getVariables();
    }

    public <V> V getValue(Variable<V> var) throws IOException {
        return loader.loadVariable(var);
    }

}
