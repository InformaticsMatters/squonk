package org.squonk.types;

import java.io.InputStream;

/** Wrapper around data from a file to allow strong typing and type conversion
 *
 *
 * @author timbo
 */
public abstract class AbstractStreamType {

    private final InputStream inputStream;


    public AbstractStreamType(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

}
