package com.im.lac.util;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A blocking queue that can be closed to signify that no new elements will be
 * added. The #close() method MUST be called to signify there will be no more
 * items or the #hasNext() method will block forever.
 *
 * @author Tim Dudgeon
 */
public class CloseableQueue<T> implements Iterator<T>, Iterable<T> {

    private static final Logger LOG = Logger.getLogger(CloseableQueue.class.getName());

    private final BlockingQueue<T> queue;
    private boolean closed = false;

    /**
     * The number of items received by the queue
     */
    private final AtomicInteger received = new AtomicInteger(0);
    /**
     * The number of items taken from the queue
     */
    private final AtomicInteger taken = new AtomicInteger(0);

    /**
     * Constructor that uses an ArrayBlockingQueue of the specified size
     *
     * @param queueSize The size at which adding new items to the queue blocks
     */
    public CloseableQueue(int queueSize) {
        queue = new ArrayBlockingQueue(queueSize);
    }

    /**
     * Constructor if you need to use your own BlockingQueue implementation
     *
     * @param queue
     */
    public CloseableQueue(BlockingQueue<T> queue) {
        this.queue = queue;
    }

    /**
     * Is the queue closed
     *
     * @return the value of closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Close so that no new values will be added
     *
     */
    public void close() {
        synchronized (queue) {
            closed = true;
        }
    }

    /**
     * Add the item to the queue
     *
     * @param item The item to add
     * @Throws IllegalStateException If the queue is already closed
     */
    public void add(T item) {
        LOG.log(Level.FINER, "add() {0}", item);
        if (closed) {
            throw new IllegalStateException("Queue is closed so can't accept new items");
        }
        try {
            queue.put(item);
            received.incrementAndGet();
        } catch (InterruptedException ex) {
            throw new RuntimeException("Interuped", ex);
        }
        LOG.log(Level.FINER, "added item{0} queue now of size {1}", new Object[]{item, queue.size()});
    }

    @Override
    public boolean hasNext() {
        LOG.finer("hasNext()");
        if (queue.size() > 0) {
            return true;
        } else if (!closed) {
            while (true) {
                LOG.finer("about to check");
                try {
                    synchronized (queue) {
                        LOG.finer("checking");
                        if (queue.size() > 0) {
                            return true;
                        } else if (closed) {
                            return false;
                        }
                    }
                    LOG.finest("waiting ..");
                    Thread.sleep(50);
                    LOG.finest("wait over");
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, "Operation interupted", ex);
                }
            }
        }
        return false;
    }

    /**
     * Retrieve the next item. Only safe to call if #hasNext() has returned true
     *
     * @return
     */
    @Override
    public T next() {
        LOG.finer("next()");
        try {
            T item = queue.take();
            LOG.log(Level.FINER, "took item {0} queue now of size {1}", new Object[]{item, queue.size()});
            taken.incrementAndGet();
            return item;
        } catch (InterruptedException ex) {
            throw new RuntimeException("Interuped", ex);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() not supported");
    }

    /**
     * Will only give the full set of items if used before the first item is
     * retrieved
     *
     * @return
     */
    @Override
    public Iterator iterator() {
        return this;
    }

    public int getReceivedCount() {
        return received.intValue();
    }

    public int getTakenCount() {
        return taken.intValue();
    }

}
