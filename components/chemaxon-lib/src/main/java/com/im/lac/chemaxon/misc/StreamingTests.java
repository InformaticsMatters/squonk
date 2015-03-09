package com.im.lac.chemaxon.misc;


import com.im.lac.util.Pool;
import chemaxon.nfunk.jep.ParseException;
import chemaxon.struc.Molecule;
import com.im.lac.chemaxon.molecule.ChemTermsEvaluator;
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils;
import com.im.lac.chemaxon.molecule.MoleculeUtils;
import com.im.lac.stream.FixedBatchSpliterator;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** This class is for testing and investigation purposes only.
 *
 * @author timbo
 */
public class StreamingTests {

    static String f = "../../data/testfiles/Building_blocks_GBP.sdf.gz";
    static long fileSize = 7003;
    //static String f = "../../data/testfiles/Screening_Collection.sdf.gz";
    //static long fileSize = 58855;
//static String f = "../../data/testfiles/nci100.smiles";
    //static int fileSize = 100;

    public static final void main(String[] args) throws Exception {
        sleep();
        noMolCreation();
        sleep();
        simpleSequential();
        sleep();
        simpleParallel();
        sleep();
        calculateSequential();
        sleep();
        calculateSequentialPool();
        sleep();
        calculateParallelPool();
        sleep();
        calculateParallelCustomPool(4);
        sleep();
        calculateParallelCustomPool(8);
        sleep();
        calculateParallelCustomPool(12);
        sleep();
        calculateParallelCustomPoolWithFixedBatchSize(8, 4);
        sleep();
        calculateParallelCustomPoolWithFixedBatchSize(8, 16);
        sleep();
        calculateParallelCustomPoolWithFixedBatchSize(8, 64);
        sleep();
        rangePrint(12, 100);
    }

    static void sleep() throws InterruptedException {
        Thread.sleep(60000);
    }

    static void noMolCreation() throws IOException {
        System.out.println("noMolCreation");
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        long results = StreamSupport.stream(r1.spliterator(), false)
                .collect(Collectors.counting());
        long t1 = System.currentTimeMillis();
        System.out.println("noMolCreation took " + (t1 - t0) + "ms. Number of records: " + results);
        IOUtils.closeIfCloseable(r1);
    }

    static void simpleSequential() throws IOException {
        System.out.println("simpleSequential");
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        long results = StreamSupport.stream(r1.spliterator(), false)
                .map(mo -> MoleculeUtils.fetchMolecule(mo, false))
                .collect(Collectors.counting());
        long t1 = System.currentTimeMillis();
        System.out.println("simpleSequential took " + (t1 - t0) + "ms");
        IOUtils.closeIfCloseable(r1);
    }

    static void simpleParallel() throws IOException {
        System.out.println("simpleParallel");
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        long results = StreamSupport.stream(r1.spliterator(), true)
                .map(mo -> MoleculeUtils.fetchMolecule(mo, false))
                .collect(Collectors.counting());
        long t1 = System.currentTimeMillis();
        System.out.println("simpleParallel took " + (t1 - t0) + "ms");
        IOUtils.closeIfCloseable(r1);
    }

    static void calculateSequential() throws Exception {
        System.out.println("calculateSequential");
        ChemTermsEvaluator cte = new ChemTermsEvaluator("logd", "logD('7.4')");
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        long results = StreamSupport.stream(r1.spliterator(), false)
                .map(mo -> MoleculeUtils.fetchMolecule(mo, false))
                .peek(mol -> {
                    cte.processMolecule(mol);
                })
                .collect(Collectors.counting());
        long t1 = System.currentTimeMillis();
        System.out.println("calculateSequential took " + (t1 - t0) + "ms");
        IOUtils.closeIfCloseable(r1);
    }

    static void calculateSequentialPool() throws Exception {
        System.out.println("calculateSequentialPool");
        EvaluatorPool pool = new EvaluatorPool(25);
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        long results = StreamSupport.stream(r1.spliterator(), false)
                .map(mo -> MoleculeUtils.fetchMolecule(mo, false))
                .peek(mol -> {
                    ChemTermsEvaluator cte = pool.checkout();
                    try {
                        cte.processMolecule(mol);
                    } finally {
                        pool.checkin(cte);
                    }
                })
                .collect(Collectors.counting());
        long t1 = System.currentTimeMillis();
        System.out.println("calculateSequentialPool took   " + (t1 - t0) + "ms");
        IOUtils.closeIfCloseable(r1);
    }

    static void calculateParallelPool() throws Exception {
        System.out.println("calculateParallelPool");
        EvaluatorPool pool = new EvaluatorPool(25);
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        long results = StreamSupport.stream(r1.spliterator(), true)
                .map(mo -> MoleculeUtils.fetchMolecule(mo, false))
                .peek(mol -> {
                    ChemTermsEvaluator cte = pool.checkout();
                    try {
                        cte.processMolecule(mol);
                    } finally {
                        pool.checkin(cte);
                    }
                })
                .collect(Collectors.counting());
        long t1 = System.currentTimeMillis();
        System.out.println("calculateParallelPool took   " + (t1 - t0) + "ms");
        IOUtils.closeIfCloseable(r1);
    }

    static void calculateParallelCustomPool(int poolSize) throws Exception {
        System.out.println("calculateParallelCustomPool " + poolSize);
        ForkJoinPool forkJoinPool = new ForkJoinPool(poolSize);
        EvaluatorPool pool = new EvaluatorPool(25);
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        System.out.println("Submitting");
        ForkJoinTask<Long> task = forkJoinPool.submit(()
                -> StreamSupport.stream(r1.spliterator(), true)
                //-> StreamSupport.stream(Spliterators.spliteratorUnknownSize(r1.iterator(), Spliterator.NONNULL), true)
                //-> StreamSupport.stream(Spliterators.spliterator(r1.iterator(), fileSize, Spliterator.NONNULL), true)
                .map(mo -> {
                    //long m0 = System.nanoTime();
                    Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
                    //long m1 = System.nanoTime();
                    //System.out.println("Molecule + " + (m1 - m0));
                    return mol;
                })
                .peek(mol -> {
                    ChemTermsEvaluator cte = pool.checkout();
                    try {
                        //long c0 = System.nanoTime();
                        cte.processMolecule(mol);
                        //long c1 = System.nanoTime();
                        //System.out.println("Calc took + " + (c1 - c0));
                    } finally {
                        pool.checkin(cte);
                    }
                })
                .collect(Collectors.counting()));
        long result = task.get();
        long t1 = System.currentTimeMillis();
        System.out.println("calculateParallelCustomPool[ " + poolSize + "] took   " + (t1 - t0) + "ms");
        IOUtils.closeIfCloseable(r1);
    }

    static void calculateParallelCustomPoolWithFixedBatchSize(int poolSize, int batchSize) throws Exception {
        System.out.println("calculateParallelCustomPoolWithFixedBatchSize " + poolSize + "," + batchSize);
        ForkJoinPool forkJoinPool = new ForkJoinPool(poolSize);
        EvaluatorPool pool = new EvaluatorPool(25);
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(f));
        long t0 = System.currentTimeMillis();
        Stream<MoleculeObject> stream = StreamSupport.stream(FixedBatchSpliterator.batchedSpliterator(r1.spliterator(), batchSize), true);

        ForkJoinTask<Long> task = forkJoinPool.submit(()
                -> stream
                .map(mo -> {
                    //long m0 = System.nanoTime();
                    Molecule mol = MoleculeUtils.fetchMolecule(mo, false);
                    //long m1 = System.nanoTime();
                    //System.out.println("Molecule + " + (m1 - m0));
                    return mol;
                })
                .peek(mol -> {
                    ChemTermsEvaluator cte = pool.checkout();
                    try {
                        //long c0 = System.nanoTime();
                        cte.processMolecule(mol);
                        //long c1 = System.nanoTime();
                        //System.out.println("Calc took + " + (c1 - c0));
                    } finally {
                        pool.checkin(cte);
                    }
                })
                .collect(Collectors.counting()));
        long result = task.get();
        long t1 = System.currentTimeMillis();
        System.out.println("calculateParallelCustomPoolWithFixedBatchSize[ " + poolSize + "," + batchSize + "] took   " + (t1 - t0) + "ms");
        IOUtils.closeIfCloseable(r1);
    }

    static void rangePrint(int poolSize, int count) throws Exception {
        System.out.println("rangePrint " + poolSize);
        ForkJoinPool forkJoinPool = new ForkJoinPool(poolSize);
        long t0 = System.currentTimeMillis();
        ForkJoinTask<Long> task = forkJoinPool.submit(()
                -> IntStream.range(1, count).parallel()
                .peek(i -> {
                    System.out.println(i + " " + Thread.currentThread());
                })
                .count());
        long result = task.get();
        long t1 = System.currentTimeMillis();
        System.out.println("rangePrint[ " + poolSize + "] took   " + (t1 - t0) + "ms");
    }

    static class EvaluatorPool extends Pool<ChemTermsEvaluator> {

        EvaluatorPool(int size) {
            super(size);
        }

        @Override
        protected ChemTermsEvaluator create() {
            System.out.println("  Pool.create() [" + Thread.currentThread() + "]");
            try {
                return new ChemTermsEvaluator("logd", "logD('7.4')");
            } catch (ParseException ex) {
                throw new RuntimeException("Failed to create", ex);
            }
        }
    }

}
