package org.squonk.io;

/**
 * Created by timbo on 27/12/16.
 */
public enum IOMultiplicity {
    /**
     * A single item
     */
    ITEM,
    /**
     * A stream of items
     */
    ARRAY,
    /**
     * Read from one of the options with the key of 'body'
     */
    OPTION
}
