package com.im.lac.services.job.variable;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author timbo
 */
public interface VariableLoader {

    public void save() throws IOException;
    
    public <V> V readFromText(String var, Class<V> type) throws IOException;
    
    public <V> V readFromJson(String var, Class<V> type) throws IOException;
    
    public InputStream readFromBytes(String var) throws IOException;
    
    public void writeToText(String var, Object o) throws IOException;
    
    public void writeToJson(String var, Object o) throws IOException;
    
    public void writeToBytes(String var, InputStream is) throws IOException;
    
    public void delete(String var) throws IOException;
    
    
    

}
