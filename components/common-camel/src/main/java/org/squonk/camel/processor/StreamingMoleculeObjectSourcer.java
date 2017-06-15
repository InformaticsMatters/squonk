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
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.camel.Exchange;

/**
 * Sources a Stream of MoleculeObjects from the exchange and dispatches them for processing. Ideally
 * the Exchanges IN message will contain a stream, but if it contains an
 * Iterator&lt;MoleculeObject&gt; or Iterable&lt;MoleculeObject&gt; these are converted to a Stream
 * using defaults (which may not be ideal).
 *
 *
 * @author Tim Dudgeon
 */
public abstract class StreamingMoleculeObjectSourcer {

    private static final Logger LOG = Logger.getLogger(StreamingMoleculeObjectSourcer.class.getName());

    public static MoleculeObjectDataset bodyAsMoleculeObjectDataset(Exchange exchange) throws IOException {

        LOG.log(Level.FINE, "Body is {0}", exchange.getIn().getBody().getClass().getName());

        MoleculeObjectDataset mods = exchange.getIn().getBody(MoleculeObjectDataset.class);
        if (mods != null) {
            return mods;
        } else {
            Stream<MoleculeObject> stream = bodyAsMoleculeObjectStream(exchange);
            if (stream != null) {
                return new MoleculeObjectDataset(new Dataset(MoleculeObject.class, stream));
            }
        }
        return null;
    }

    public static Stream<MoleculeObject> bodyAsMoleculeObjectStream(Exchange exchange) throws IOException {

        LOG.log(Level.FINE, "Body is {0}", exchange.getIn().getBody().getClass().getName());

        MoleculeObjectDataset mods = exchange.getIn().getBody(MoleculeObjectDataset.class);
        if (mods != null) {
            return mods.getDataset().getStream();
        }
        StreamProvider sp = exchange.getIn().getBody(StreamProvider.class);
        if (sp != null) {
            if (sp.getType() != MoleculeObject.class) {
                throw new IllegalStateException("Stream doesn't contain MoleculeObjects");
            }
            return sp.getStream();
        }
        Stream<MoleculeObject> stream = exchange.getIn().getBody(Stream.class);
        // this is not really safe as it could be a Stream of bananas. 
        // advise to use StreamProvider
        if (stream != null) {
            LOG.info("Unsafe use of raw stream. Better to use StreamProvider");
            return stream;
        }

        Iterator<MoleculeObject> moiterator = null;
        MoleculeObjectIterable moi = exchange.getIn().getBody(MoleculeObjectIterable.class);
        if (moi != null) {
            moiterator = moi.iterator();
        } else {
            Iterator it = exchange.getIn().getBody(Iterator.class);
            if (it != null) {
                moiterator = it;
            } else {
                Iterable itb = exchange.getIn().getBody(Iterable.class);
                if (itb != null) {
                    moiterator = itb.iterator();
                }
            }
        }
        if (moiterator != null) {
            LOG.info("Unsafe or deprecated use of Iterable/Iterator. Better to use StreamProvider");
            Spliterator spliterator = Spliterators.spliteratorUnknownSize(moiterator, Spliterator.NONNULL | Spliterator.ORDERED);
            return StreamSupport.stream(spliterator, true);
        }

        return null;
    }

    public void handle(Exchange exchange) throws Exception {
        MoleculeObject mol = exchange.getIn().getBody(MoleculeObject.class);
        if (mol != null) {
            handleSingle(exchange, mol);
            return;
        }
        Stream<MoleculeObject> stream = bodyAsMoleculeObjectStream(exchange);
        if (stream != null) {
            handleMultiple(exchange, stream);
            return;
        }
        // give up
        handleOther(exchange, exchange.getIn().getBody());

    }

    public abstract void handleSingle(Exchange exchange, MoleculeObject mol) throws Exception;

    public abstract void handleMultiple(Exchange exchange, Stream<MoleculeObject> mols) throws Exception;

    public void handleOther(Exchange exchange, Object o) {
        LOG.log(Level.WARNING, "Can't find molecules from {0}", o.getClass().getName());
        throw new IllegalArgumentException("No valid MoleculeObject content could be found");
    }

}
