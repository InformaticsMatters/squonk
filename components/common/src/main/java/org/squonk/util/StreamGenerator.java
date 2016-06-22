package org.squonk.util;

import java.io.IOException;
import java.util.stream.Stream;


/**
 * Wraps an Stream&lt;MoleculeObject&gt; to allow stronger typing and allow some
 * flexibility in the type of Stream generated.
 * Note that some stream implementations will need to be closed as they use underlying resources
 * The golden run is if you are performing the terminal operation on the stream you MUST
 * close it. A good way to do this is in a try-with-resources statement.
 *
 * @author timbo
 */
public interface StreamGenerator<T> extends StreamProvider<T> {
        
    Stream<T> getStream(boolean parallel, int batchSize) throws IOException;
    
    Stream<T> getStream(boolean parallel) throws IOException;
    
}
