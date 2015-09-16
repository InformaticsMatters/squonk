package com.squonk.types;

import java.io.InputStream;

/** Wrapper around data from an SD file to allow strong typing and type conversion
 * 
 * TODO - handle ability to name the file and provide other info
 *
 * @author timbo
 */
public class SDFile {
    
    private final InputStream inputStream;
    
    public SDFile(InputStream inputStream) {
        this.inputStream = inputStream;                
    }

    public InputStream getInputStream() {
        return inputStream;
    }
    
}
