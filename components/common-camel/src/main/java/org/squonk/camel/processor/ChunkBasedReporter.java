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

package org.squonk.camel.processor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author timbo
 */
public class ChunkBasedReporter implements Processor {

    private static final Logger LOG = Logger.getLogger(ChunkBasedReporter.class.getName());
    private int count = 0;
    private long start = 0;
    int chunkSize = 1000;
    
    public ChunkBasedReporter() {
        
    }
    
    public ChunkBasedReporter(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void process(Exchange ex) {
        if (start == 0) {
            start = System.currentTimeMillis();
        }
        count++;
        if (count % chunkSize == 0) {
            long now = System.currentTimeMillis();
            long duration = (long) ((float) (now - start) / 1000f);
            LOG.log(Level.INFO, "{0} records processed in {1} sec. Total is now {2}", new Object[]{chunkSize, duration, count});
            start = now;
        }
    }
}
