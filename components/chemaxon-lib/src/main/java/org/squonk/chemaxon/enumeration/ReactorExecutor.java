package org.squonk.chemaxon.enumeration;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.io.MPropHandler;
import chemaxon.reaction.ConcurrentReactorProcessor;
import chemaxon.reaction.ReactionException;
import chemaxon.reaction.Reactor;
import chemaxon.struc.MProp;
import chemaxon.struc.MPropertyContainer;
import chemaxon.struc.Molecule;
import chemaxon.util.iterator.MoleculeIterator;
import chemaxon.util.iterator.MoleculeIteratorFactory;
import org.squonk.chemaxon.molecule.MoleculeUtils;
import org.squonk.types.MoleculeObject;
import org.squonk.util.Metrics;
import org.squonk.util.StatsRecorder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicInteger;
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

    public static final String R1_REACTANT = "R1_REACTANT";
    public static final String R2_REACTANT = "R2_REACTANT";
    public static final String R1_INDEX = "R2_INDEX";
    public static final String R2_INDEX = "R2_INDEX";

    private static final Logger LOG = Logger.getLogger(ReactorExecutor.class.getName());

    private final Molecule reaction;
    private final StatsRecorder statsRecorder;

    public ReactorExecutor(Molecule reaction) {
        this(reaction, null);
    }

    public ReactorExecutor(Molecule reaction, StatsRecorder statsRecorder) {
        this.reaction = reaction;
        this.statsRecorder = statsRecorder;
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

    /** Enumerate a reaction using the specified reactants
     *
     * @param output what to output
     * @param ignoreRules whether to ignore the reaction rules
     * @param sources The reactants to enumerate
     * @return
     * @throws ReactionException
     * @throws IOException
     */
    public Stream<MoleculeObject> enumerate(Output output, int ignoreRules, Molecule[][] sources) throws ReactionException, IOException {

        Spliterator spliterator = new ReactorSpliterator(reaction, output, ignoreRules, sources);
        return StreamSupport.stream(spliterator, true);
    }

    public Stream<MoleculeObject> enumerateMoleculeObjects(Output output, int ignoreRules, Stream<MoleculeObject> reactants) throws ReactionException, IOException {

        Stream<Molecule> mols = reactants.map((mo) -> MoleculeUtils.cloneMolecule(mo, true));
        return enumerateMolecules( output, ignoreRules, mols);
    }
        /**  Enumerate a reaction using the specified reactants.
         * The reactants are specified in the Stream of Molecules. The nature of the reactant is specified by fields named
         * R1_REACTANT and R2_REACTANT. If a field named R1_REACTANT is present the molecule is handled as one of the R1's and
         * if a field named REACTANT_R2 is present the molecule is handled as one of the R2's. Both fields can be present. If
         * the R1/2_REACTANT fields have a value that value is added to resulting output molecules with the corresponding field name.
         * Fields named R1_INDEX AND R2_INDEX are also added with the values of the reactant index in the order they were specified,
         * starting from 1.
         *
         * @param output what to output
         * @param ignoreRules whether to ignore the reaction rules
         * @param reactants the reactants to react (see above for details)
         * @return
         * @throws ReactionException
         * @throws IOException
         */
    public Stream<MoleculeObject> enumerateMolecules(Output output, int ignoreRules, Stream<Molecule> reactants) throws ReactionException, IOException {
        ;
        List<Molecule> r1s = new ArrayList<>();
        List<Molecule> r2s = new ArrayList<>();
       reactants.forEach((m) -> {
            MPropertyContainer container = m.properties();

            MProp r1 = container.get(R1_REACTANT);
            MProp r2 = container.get(R2_REACTANT);
            if (r1 != null) {
                r1s.add(m);
            }
            if (r2 != null) {
                r2s.add(m);
            }
        });
        LOG.info(String.format("Found %s R1s and %s R2s", r1s.size(), r2s.size()));
        Molecule[] mols1 = r1s.toArray(new Molecule[r1s.size()]);
        Molecule[] mols2 = r2s.toArray(new Molecule[r2s.size()]);

        ReactorSpliterator spliterator = new ReactorSpliterator(reaction, output, ignoreRules, new Molecule[][] {mols1, mols2});
        return StreamSupport.stream(spliterator, true).onClose(() -> {
            int total = spliterator.count.get();
            if (statsRecorder != null && total > 0) {
                statsRecorder.recordStats(Metrics.generate(Metrics.PROVIDER_CHEMAXON, Metrics.METRICS_RXN_ENUM), total);
            }
        });
    }

    class ReactorSpliterator extends AbstractSpliterator<MoleculeObject> {

        final Reactor reactor;
        final ConcurrentReactorProcessor crp;
        final AtomicInteger count = new AtomicInteger(0);

        ReactorSpliterator(Molecule reaction, Output output, int ignoreRules, Molecule[][] sources) throws ReactionException, IOException {
            super(Long.MAX_VALUE, Spliterator.NONNULL);
            reactor = createReactor(reaction, output, ignoreRules);
            crp = new ConcurrentReactorProcessor();
            crp.setReactor(reactor);
            addReactantIndexes(sources);
            MoleculeIterator[] reactantIterators = createReactantIterators(sources);
            crp.setReactantIterators(reactantIterators, ConcurrentReactorProcessor.MODE_COMBINATORIAL);
        }

        private void addReactantIndexes(Molecule[][] mols) {
            int index = 1;
            for (Molecule[] inner: mols) {
                addReactantIndexes(inner, index++);
            }
        }

        private void addReactantIndexes(Molecule[] mols, int reactantNum) {
            int index = 1;
            for (Molecule mol: mols) {
                mol.setProperty("R" + reactantNum + "_INDEX", "" + index++);
            }
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
                //LOG.info("Processing reactor output");
                for (Molecule product : products) {
                    try {
                        MoleculeObject mo = MoleculeUtils.createMoleculeObject(product, "mol");
                        int i = 1;
                        for (Molecule reactant : reactants) {
                            String r = "R" + i + "_";
                            for (String key : reactant.properties().getKeys()) {
                                Object val = reactant.getPropertyObject(key);
                                if (key.startsWith(r)) {
                                    mo.putValue(key, val);
                                } else if (key.matches("R\\d+_.*")) {
                                    // skip property as it relates to different reactant
                                } else {
                                    mo.putValue(r + key, val);
                                }
                            }
                            i++;
                        }
                        action.accept(mo);
                        count.incrementAndGet();
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
