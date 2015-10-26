package com.im.lac.services.job.variable;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author timbo
 */
public interface VariableLoader {

    public <V> V loadVariable(Variable<V> var) throws IOException;

    public <V> void saveVariable(Variable<V> var, V value) throws IOException;

    public <V> void writeVariable(Variable<V> var, InputStream is) throws IOException;

    public <V> void writeVariable(Variable<V> var, String s) throws IOException;

    public void save() throws IOException;

}
