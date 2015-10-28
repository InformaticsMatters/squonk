package com.im.lac.services.job.variable;

import java.io.IOException;
import java.util.Set;

/**
 *
 * @author timbo
 */
public interface VariableLoader {

    public Set<Variable> getVariables();
    
    public Variable lookupVariable(String name);
    
    public <V> V loadVariable(Variable<V> var) throws IOException;
    
    public <V> void writeVariable(Variable<V> var, V value) throws IOException;

    public void save() throws IOException;

}
