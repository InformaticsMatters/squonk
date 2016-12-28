package org.squonk.io;

/**
 * Created by timbo on 27/12/16.
 */
public enum IORoute {
    /**
     * A file in the file system, or an attachment for an HTTP request
     */
    FILE,
    /**
     * STDIN/STDOUT or the body of a HTTP POST or response
     */
    STREAM
}
