package org.squonk.core;

/**
 *
 * @author timbo
 */
public class IncompatibleDataException extends Exception {

    /**
     * Creates a new instance of <code>IncompatibleDataException</code> without
     * detail message.
     */
    public IncompatibleDataException() {
    }

    /**
     * Constructs an instance of <code>IncompatibleDataException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IncompatibleDataException(String msg) {
        super(msg);
    }
}
