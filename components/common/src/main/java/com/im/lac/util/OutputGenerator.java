package com.im.lac.util;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author timbo
 */
public interface OutputGenerator {
    
    public InputStream getTextStream(String format) throws IOException;
    
}
