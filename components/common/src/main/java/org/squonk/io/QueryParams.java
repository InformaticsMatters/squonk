package org.squonk.io;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by timbo on 29/09/2016.
 */
public class QueryParams {

    private final List<Pair> pairs = new ArrayList<>();

    public void add(String key, String value) {
        pairs.add(new Pair(key, value));
    }

    public List<Pair> getPairs() { return pairs; }

    public void consume(BiConsumer<String,String> consumer) {
        pairs.stream().peek((p) -> consumer.accept(p.getKey(), p.getValue()));
    }

    public class Pair {
        String key, value;
        Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}
