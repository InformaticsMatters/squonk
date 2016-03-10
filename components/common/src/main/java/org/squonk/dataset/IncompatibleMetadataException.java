package org.squonk.dataset;

/**
 * Created by timbo on 08/03/16.
 */
public class IncompatibleMetadataException extends Exception  {

    public IncompatibleMetadataException(String reason) {
        super(reason);
    }

    public IncompatibleMetadataException(String reason, Throwable ex) {
        super(reason, ex);
    }
}
