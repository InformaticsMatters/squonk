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

import org.squonk.stream.FixedBatchSpliterator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Type safe simple stream provider that is just a wrapper around an existing Stream or Iterator
 *
 * @author timbo
 */
public class SimpleStreamProvider<T> implements StreamProvider, Iterable {

    final private Stream<T> stream;
    final private Class<T> type;

    public SimpleStreamProvider(Stream<T> stream, Class<T> type) {
        this.stream = stream;
        this.type = type;
    }

    /**
     * Create a Steam from the Iterator with default characteristics of Spliterator.NONNULL |
     * Spliterator.ORDERED
     *
     * @param iter
     * @param type
     */
    public SimpleStreamProvider(Iterator<T> iter, Class<T> type) {
        this(iter, type, Spliterator.NONNULL | Spliterator.ORDERED);
    }

    /** Create a Stream with the specified characteristics
     * 
     * @param iter
     * @param type
     * @param characteristics 
     */
    public SimpleStreamProvider(Iterator<T> iter, Class<T> type, int characteristics) {
        Spliterator spliterator = Spliterators.spliteratorUnknownSize(iter, characteristics);
        stream = StreamSupport.stream(spliterator, true);
        this.type = type;
    }
    
    /** Create a Stream with the specified characteristics and batch size. The default 
     * batch size used by Java can be quite large resulting in poor parallelization, so
     * it can be of benefit to set this to a relatively low level.
     * 
     * @param iter
     * @param type
     * @param characteristics
     * @param batchSize 
     */
    public SimpleStreamProvider(Iterator<T> iter, Class<T> type, int characteristics, int batchSize) {
        Spliterator toWrap = Spliterators.spliteratorUnknownSize(iter, characteristics);
        Spliterator spliterator = new FixedBatchSpliterator(toWrap, batchSize);
        stream = StreamSupport.stream(spliterator, true);
        this.type = type;
    }

    @Override
    public Stream<T> getStream() {
        return stream;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public Iterator<T> iterator() {
        return getStream().iterator();
    }

}
