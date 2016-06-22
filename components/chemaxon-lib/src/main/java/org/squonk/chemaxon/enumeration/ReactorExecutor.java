package org.squonk.chemaxon.enumeration;

import chemaxon.reaction.ConcurrentReactorProcessor;
import chemaxon.reaction.ReactionException;
import chemaxon.reaction.Reactor;
import chemaxon.struc.Molecule;
import chemaxon.util.iterator.MoleculeIterator;
import chemaxon.util.iterator.MoleculeIteratorFactory;
import org.squonk.chemaxon.molecule.MoleculeUtils;
import org.squonk.types.MoleculeObject;
import java.io.IOException;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.logging.Level;
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

    public Stream<MoleculeObject> enumerate(Molecule reaction, Output output, int ignoreRules, Molecule[][] sources) throws ReactionException, IOException {

        Spliterator spliterator = new ReactorSpliterator(reaction, output, ignoreRules, sources);
        return StreamSupport.stream(spliterator, true);
    }

    class ReactorSpliterator extends AbstractSpliterator<MoleculeObject> {

        final Reactor reactor;
        final ConcurrentReactorProcessor crp;

        ReactorSpliterator(Molecule reaction, Output output, int ignoreRules, Molecule[][] sources) throws ReactionException, IOException {
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
            Molecule[] reactants;
            try {
                products = crp.react();
                reactants = crp.getReactants();
            } catch (ReactionException ex) {
                throw new RuntimeException(ex);
            }
            if (products != null) {
                for (Molecule product : products) {
                    try {
                        MoleculeObject mo = MoleculeUtils.createMoleculeObject(product, "mol");
                        int i = 1;
                        for (Molecule reactant : reactants) {
                            for (String key : reactant.properties().getKeys()) {
                                Object val = reactant.getPropertyObject(key);
                                mo.putValue("R" + i + "_" + key, val);
                                i++;
                            }
                        }
                        action.accept(mo);
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Failed to write molecule", ex);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private MoleculeIterator[] createReactantIterators(Molecule[]... sources) {
        MoleculeIterator[] iterators = new MoleculeIterator[sources.length];
        int i = 0;
        for (Molecule[] mols : sources) {
            iterators[i] = MoleculeIteratorFactory.createMoleculeIterator(mols);
            i++;
        }
        return iterators;
    }

}
