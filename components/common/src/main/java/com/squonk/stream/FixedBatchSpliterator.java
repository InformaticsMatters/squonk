package com.squonk.stream;

import static java.util.stream.StreamSupport.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FixedBatchSpliterator<T> extends FixedBatchSpliteratorBase<T> {

    private final Spliterator<T> spliterator;

    public FixedBatchSpliterator(Spliterator<T> toWrap, int batchSize) {
        super(toWrap.characteristics(), batchSize, toWrap.estimateSize());
        this.spliterator = toWrap;
    }

    public static <T> FixedBatchSpliterator<T> batchedSpliterator(Spliterator<T> toWrap, int batchSize) {
        return new FixedBatchSpliterator<>(toWrap, batchSize);
    }

    public static <T> Stream<T> withBatchSize(Stream<T> in, int batchSize) {
        return stream(batchedSpliterator(in.spliterator(), batchSize), true);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        return spliterator.tryAdvance(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        spliterator.forEachRemaining(action);
    }
}
