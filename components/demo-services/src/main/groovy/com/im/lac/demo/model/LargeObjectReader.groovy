package com.im.lac.demo.model

/**
 *
 * @author timbo
 */
interface LargeObjectReader {
    
    InputStream getInputStream()
    void close()
	
}

