package com.im.lac.types;


/**
 * Interface that wraps an Iterable<MoleculeObject> to allow stronger typing.
 * Note that some implementations will implement Closeable as they may use underlying resources that
 * need to be closed, so whenever an instance is finished with you should check if 
 * the instance implements Closeable and if so call close().
 *
 * @author timbo
 */
public interface MoleculeObjectIterable extends Iterable<MoleculeObject> {
    
}
