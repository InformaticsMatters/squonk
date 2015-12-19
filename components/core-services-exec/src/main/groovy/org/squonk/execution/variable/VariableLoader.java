package org.squonk.execution.variable;

import com.squonk.dataset.Dataset;
import org.squonk.notebook.api.VariableKey;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author timbo
 */
public interface VariableLoader {

    public void save() throws IOException;
    
    public <V> V readFromText(VariableKey var, Class<V> type) throws IOException;
    
    public <V> V readFromJson(VariableKey var, Class<V> type) throws IOException;
    
    public InputStream readBytes(VariableKey var, String label) throws IOException;
    
    public void writeToText(VariableKey var, Object o) throws IOException;
    
    public void writeToJson(VariableKey var, Object o) throws IOException;
    
    public void writeToBytes(VariableKey var, String label, InputStream is) throws IOException;

}
