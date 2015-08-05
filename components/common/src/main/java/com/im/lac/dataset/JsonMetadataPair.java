package com.im.lac.dataset;

import java.io.InputStream;

/**
 * A holder for an InputStream containing JSON and a Metadata object describing it. 
 * 
 * @author timbo
 */
public class JsonMetadataPair {
    
    final InputStream inputStream;
    final Metadata metadata;

    public JsonMetadataPair(InputStream inputStream, Metadata metadata) {
        this.inputStream = inputStream;
        this.metadata = metadata;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Metadata getMetadata() {
        return metadata;
    }
    
}
