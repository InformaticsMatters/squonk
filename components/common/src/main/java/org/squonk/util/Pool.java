/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.util;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Generic pool for things.
 * Created for the purpose of having a pool of non-threadsafe objects that can be used 
 * from a thread pool, but may have other more general uses.
 * Usage:
 * 1. Subclass and override the create() method to create your instances.
 * 2. Create new instance, specifying the size of the pool
 * 3. Get hold of an instance using the checkout() method.
 * 4. Perform work using the instance.
 * 5. Return the instance to the pool using the checkin() method when finished.
 * 
 * Note: the size parameter specifies the maximum number of instance that will be 
 * retained when checked in. The total number of instances is unlimited, with new ones
 * being created if none have been checked back in.
 * Note: instances held by the pool will not be released until the pool instance is 
 * garbage collected e.g. there is no timeout function. 
 *
 * @author timbo
 */
public abstract class Pool<T> {

    ArrayBlockingQueue<T> q;

    public Pool(int size) {
        q = new ArrayBlockingQueue<>(size);
    }

    /**
     * Get hold of an instance, creating a new one if none are free.
     * @return 
     */
    public T checkout() {
        T item = q.poll();
        if (item == null) {
            item = create();
        }
        //System.out.println("  Pool.checkout() [" + Thread.currentThread() + "] " + item);
        return item;
    }

    /**
     * Return an item to the pool. 
     * Preferably do this in a finally {} block to ensure it is returned
     * @param item 
     */
    public void checkin(T item) {
        //System.out.println("  Pool.checkin() [" + Thread.currentThread() + "] " + item);
        q.offer(item);
    }

    /**
     * Override to create your instances.
     * @return 
     */
    protected abstract T create();

}
