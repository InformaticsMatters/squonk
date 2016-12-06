package org.squonk.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 05/12/16.
 */
public interface Resource {
    InputStream get() throws IOException;
}
