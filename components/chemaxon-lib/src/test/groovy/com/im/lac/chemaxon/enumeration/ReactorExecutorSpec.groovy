package com.im.lac.chemaxon.enumeration

import spock.lang.Specification
import chemaxon.formats.MolImporter
import chemaxon.util.iterator.MoleculeIteratorFactory
import chemaxon.struc.Molecule
import chemaxon.reaction.Reactor
import chemaxon.reaction.ConcurrentReactorProcessor
import com.im.lac.chemaxon.molecule.MoleculeObjectUtils
import com.im.lac.types.MoleculeObjectIterable
import com.im.lac.util.CollectionUtils
import com.im.lac.chemaxon.molecule.MoleculeUtils
import java.util.stream.StreamSupport
import java.util.stream.Collectors

/**
 *
 * @author timbo
 */
class ReactorExecutorSpec extends Specification {
    
    String reactants = "../../data/testfiles/nci100.smiles"
    String reaction = "../../data/testfiles/amine-acylation.mrv"
    
    void "simple enumerate"() {
        
        setup:
        println "simple enumerate"
        Molecule rxn = MolImporter.importMol(new File(reaction).text)
        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File(reactants))
        MoleculeObjectIterable r2 = MoleculeObjectUtils.createIterable(new File(reactants))
        ReactorExecutor exec = new ReactorExecutor()
        
        when:
        println "reacting"
        long t0 = System.currentTimeMillis()
        def results = exec.enumerate(rxn, ReactorExecutor.Output.Product1, Reactor.IGNORE_REACTIVITY | Reactor.IGNORE_SELECTIVITY, r1, r2)
        println "collecting"
        def list = results.collect(Collectors.toList())
        long t1 = System.currentTimeMillis()
        println "Number of products: ${list.size()} generated in ${t1-t0}ms"
                
        then:
        list.size() > 0
        
        cleanup:
        r1?.close()
        r2?.close()
    }
    
    void "chemaxon concurrent reactor"() {
        setup:
        println "chemaxon concurrent reactor"
        Molecule rxnmol = new MolImporter(reaction).read();
        MolImporter[] importers = [new MolImporter(reactants), new MolImporter(reactants)] as MolImporter[]
        Reactor reactor = new Reactor();
        reactor.setIgnoreRules(Reactor.IGNORE_REACTIVITY | Reactor.IGNORE_SELECTIVITY);
        reactor.setReaction(rxnmol);
        ConcurrentReactorProcessor crp = new ConcurrentReactorProcessor();
        crp.setReactor(reactor);
        crp.setReactantIterators(MoleculeIteratorFactory.getMoleculeIterators(importers), ConcurrentReactorProcessor.MODE_COMBINATORIAL);
        
        when:
        int count = 0
        Molecule[] products
        long t0 = System.currentTimeMillis()
        while ((products = crp.react()) != null) {
            for (Molecule product : products) {
                count++
            }
        }
        long t1 = System.currentTimeMillis()
        println "Number of products: $count generated in ${t1-t0}ms"
 
        then:
        count > 0;
        
        cleanup:
        importers.each { it.close() }
    }
    
//    void "combinatorialIterator speed"() {
//        
//        setup:
//        println "combinatorialIterator speed"
//        MoleculeObjectIterable r1 = MoleculeObjectUtils.createIterable(new File("../../data/testfiles/nci100.smiles"))
//        MoleculeObjectIterable r2 = MoleculeObjectUtils.createIterable(new File("../../data/testfiles/nci100.smiles"))
//        
//        when:
//        long t0 = System.currentTimeMillis()
//        def it = CollectionUtils.combinatorialIterator(25, r1, r2);
//        def list = it.collect()
//        long t1 = System.currentTimeMillis()
//        
//        then:
//        println "Number of products: ${list.size()} generated in ${t1-t0}ms"
//        list.size() == 10000
//        
//        cleanup:
//        r1?.close()
//        r2?.close()
//    
//    }
    
}

