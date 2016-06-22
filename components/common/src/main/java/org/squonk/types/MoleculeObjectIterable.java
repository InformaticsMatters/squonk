package org.squonk.types;


import org.squonk.types.MoleculeObject;

/**
 * Interface that wraps an Iterable&lt;MoleculeObject&gt; to allow stronger typing.
 * Note that some implementations will implement Closeable as they may use underlying resources that
 * need to be closed, so whenever an instance is finished with you should check if 
 * the instance implements Closeable and if so call close().
 * 
 * Note: Iterables/Iterators are generally being phased out in preference to Streams.
 *
 * @author timbo
 * @ see {@link MoleculeObjectStream}
 */
public interface MoleculeObjectIterable extends Iterable<MoleculeObject> {
    
}
