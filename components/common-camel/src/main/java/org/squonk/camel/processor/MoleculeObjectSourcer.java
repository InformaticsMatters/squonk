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

import org.squonk.types.MoleculeObject;
import org.squonk.types.MoleculeObjectIterable;
import org.squonk.util.StreamProvider;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.camel.Exchange;

/**
 *
 * @author Tim Dudgeon
 */
public abstract class MoleculeObjectSourcer {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectSourcer.class.getName());

    public static Iterator<MoleculeObject> bodyAsMoleculeObjectIterator(Exchange exchange) throws IOException {
        StreamProvider sp = exchange.getIn().getBody(StreamProvider.class);
        if (sp != null) {
            if (sp.getType() != MoleculeObject.class) {
                throw new IllegalStateException("Stream doesn't contain MoleculeObjects");
            }
            return sp.getStream().iterator();
        }
        Stream<MoleculeObject> stream = exchange.getIn().getBody(Stream.class);
        if (stream != null) {
            return stream.iterator();
        }
        Iterable<MoleculeObject> iterable = exchange.getIn().getBody(MoleculeObjectIterable.class);
        if (iterable == null) {
            iterable = exchange.getIn().getBody(Iterable.class);
        }
        if (iterable != null) {
            return iterable.iterator();
        }
        Iterator<MoleculeObject> iterator = exchange.getIn().getBody(Iterator.class);
        return iterator;
    }

    public void handle(Exchange exchange) throws Exception {
        Iterator<MoleculeObject> iterator = bodyAsMoleculeObjectIterator(exchange);
        if (iterator != null) {
            handleMultiple(exchange, iterator);
        } else {
            MoleculeObject mol = exchange.getIn().getBody(MoleculeObject.class);
            if (mol != null) {
                handleSingle(exchange, mol);
            } else {
                // give up
                handleOther(exchange, exchange.getIn().getBody());
            }
        }
    }

    public abstract void handleSingle(Exchange exchange, MoleculeObject mol) throws Exception;

    public abstract void handleMultiple(Exchange exchange, Iterator<MoleculeObject> mols) throws Exception;

    public void handleOther(Exchange exchange, Object o) {
        LOG.log(Level.WARNING, "Can't find molecules from {0}", o.getClass().getName());
        throw new IllegalArgumentException("No valid MoleculeObject content could be found");
    }

}
