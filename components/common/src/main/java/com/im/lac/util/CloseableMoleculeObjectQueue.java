package com.im.lac.util;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author timbo
 */
public class CloseableMoleculeObjectQueue extends CloseableQueue<MoleculeObject> implements MoleculeObjectIterable {

    public CloseableMoleculeObjectQueue(int queueSize) {
        super(queueSize);
    }

    public CloseableMoleculeObjectQueue(BlockingQueue<MoleculeObject> queue) {
        super(queue);
    }

}
