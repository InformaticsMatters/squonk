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
        pairs.forEach((p) -> consumer.accept(p.getKey(), p.getValue()));
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
