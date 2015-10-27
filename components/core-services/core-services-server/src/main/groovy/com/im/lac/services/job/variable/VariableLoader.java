package com.im.lac.services.job.variable;

import java.io.IOException;

/**
 *
 * @author timbo
 */
public interface VariableLoader {

    public <V> V loadVariable(Variable<V> var) throws IOException;
    
    public <V> void writeVariable(Variable<V> var, V value) throws IOException;

    public void save() throws IOException;

}
