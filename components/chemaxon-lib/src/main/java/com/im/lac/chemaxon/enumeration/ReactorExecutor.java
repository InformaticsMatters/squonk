package com.im.lac.chemaxon.enumeration;

import chemaxon.reaction.ConcurrentReactorProcessor;
import chemaxon.reaction.ReactionException;
import chemaxon.reaction.Reactor;
import chemaxon.struc.Molecule;
import chemaxon.util.iterator.MoleculeIterator;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.types.MoleculeObject;
import com.im.lac.util.IOUtils;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author timbo
 */
public class ReactorExecutor {

    public enum Output {

        Reaction, Product1, Product2, Product3, Product4, Product5
    }

    private static final Logger LOG = Logger.getLogger(ReactorExecutor.class.getName());

    public ReactorExecutor() {

    }

    Reactor createReactor(Molecule reaction, Output output, int ignoreRules) throws ReactionException {
        final Reactor reactor = new Reactor();
        reactor.setReaction(reaction);
        reactor.setIgnoreRules(ignoreRules);
        switch (output) {
            case Reaction:
                reactor.setResultType(Reactor.REACTION_OUTPUT);
                break;
            default:
                reactor.setResultType(Reactor.PRODUCT_OUTPUT);
        }
        return reactor;
    }

    public Stream<MoleculeObject> enumerate(Molecule reaction, Output output, int ignoreRules, Iterable<MoleculeObject>... sources) throws ReactionException, IOException {
        
        Spliterator spliterator = new ReactorSpliterator(reaction, output, ignoreRules, sources);
        return StreamSupport.stream(spliterator, true);
    }

    class ReactorSpliterator extends AbstractSpliterator<MoleculeObject> {

        final Reactor reactor;
        final ConcurrentReactorProcessor crp;

        ReactorSpliterator(Molecule reaction, Output output, int ignoreRules, Iterable<MoleculeObject>... sources) throws ReactionException, IOException {
            super(Long.MAX_VALUE, Spliterator.NONNULL);
            reactor = createReactor(reaction, output, ignoreRules);
            crp = new ConcurrentReactorProcessor();
            crp.setReactor(reactor);
            MoleculeIterator[] reactantIterators = createReactantIterators(sources);
            crp.setReactantIterators(reactantIterators, ConcurrentReactorProcessor.MODE_COMBINATORIAL);
        }

        @Override
        public boolean tryAdvance(Consumer action) {

            Molecule[] products;
            try {
                products = crp.react();
            } catch (ReactionException ex) {
                throw new RuntimeException(ex);
            }
            if (products != null) {
                for (Molecule product : products) {
                    action.accept(product);
                }
                return true;
            }
            return false;
        }
    }

    private MoleculeIterator[] createReactantIterators(Iterable<MoleculeObject>... sources) {
        MoleculeIterator[] iterators = new MoleculeIterator[sources.length];
        int i = 0;
        for (Iterable<MoleculeObject> source : sources) {
            iterators[i++] = new MoleculeIteratorAdapter(source);
        }
        return iterators;
    }

    private class MoleculeIteratorAdapter implements MoleculeIterator {

        Iterator<MoleculeObject> iterator;
        Iterable<MoleculeObject> iterable;

        MoleculeIteratorAdapter(Iterable<MoleculeObject> iterable) {
            this.iterable = iterable;
            this.iterator = iterable.iterator();
        }

        @Override
        public Molecule next() {
            MoleculeObject mo = iterator.next();
            if (mo != null) {
                return MoleculeUtils.cloneMolecule(mo, true);
            } else {
                throw new NoSuchElementException("No more Molecules");
            }
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Throwable getThrowable() {
            return null;
        }

        @Override
        public double estimateProgress() {
            return 0;
        }

        void close() {
            IOUtils.closeIfCloseable(iterable);
        }

    }

}
