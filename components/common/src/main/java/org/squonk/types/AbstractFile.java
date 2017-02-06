package org.squonk.types;

import java.io.InputStream;

/** Wrapper around data from a file to allow strong typing and type conversion
 *
 *
 * @author timbo
 */
public abstract class AbstractFile {

    private final InputStream inputStream;


    public AbstractFile(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

}
