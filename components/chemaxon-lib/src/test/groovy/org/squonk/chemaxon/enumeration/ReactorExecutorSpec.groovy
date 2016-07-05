package org.squonk.chemaxon.enumeration

import chemaxon.formats.MolImporter
import chemaxon.reaction.ConcurrentReactorProcessor
import chemaxon.reaction.Reactor
import chemaxon.struc.Molecule
import chemaxon.util.iterator.MoleculeIteratorFactory
import org.squonk.chemaxon.molecule.MoleculeObjectUtils
import org.squonk.chemaxon.molecule.MoleculeUtils
import org.squonk.data.Molecules
import org.squonk.types.MoleculeObject
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 *
 * @author timbo
 */
class ReactorExecutorSpec extends Specification {
    
    String reactants = "../../data/testfiles/nci100.smiles"
    String reaction = "../../data/testfiles/amine-acylation.mrv"
    
    Molecule[] getMoleculeArrayFromFile(FileInputStream fis) {
        Stream<MoleculeObject> stream = MoleculeObjectUtils.createStreamGenerator(fis).getStream(true);
        try {          
            List<Molecule> mols = stream.collect {
                MoleculeUtils.cloneMolecule(it, true)
            }
            return mols.toArray(new Molecule[0]);
        } finally {
            stream.close()   
        }
    }
        
    
    void "simple enumerate"() {
        
        setup:
        Molecule rxn = MolImporter.importMol(new File(reaction).text)
        Molecule[] r1 = getMoleculeArrayFromFile(new FileInputStream(reactants))     
        Molecule[] r2 = getMoleculeArrayFromFile(new FileInputStream(reactants))
        ReactorExecutor exec = new ReactorExecutor(rxn)
        
        when:
        //long t0 = System.currentTimeMillis()
        def results = exec.enumerate(ReactorExecutor.Output.Product1, Reactor.IGNORE_REACTIVITY | Reactor.IGNORE_SELECTIVITY, r1, r2)
        def list = results.collect(Collectors.toList())
        //long t1 = System.currentTimeMillis()
        //println "Number of products: ${list.size()} generated in ${t1-t0}ms"
                
        then:
        list.size() > 0
    }

    void "enumerate as stream"() {

        setup:
        Molecule rxn = MolImporter.importMol(new File(reaction).text)
        List mols = Molecules.nci100Molecules()
        mols.eachWithIndex { mo, c ->
            mo.putValue("R1_REACTANT", "R1_" + (c+1))
            mo.putValue("R2_REACTANT", "R2_" + (c+1))
        }


        ReactorExecutor exec = new ReactorExecutor(rxn)

        when:
        //long t0 = System.currentTimeMillis()
        def results = exec.enumerateMoleculeObjects(ReactorExecutor.Output.Product1, Reactor.IGNORE_REACTIVITY | Reactor.IGNORE_SELECTIVITY, mols.stream())
        def list = results.collect(Collectors.toList())
        //long t1 = System.currentTimeMillis()
        //println "Number of products: ${list.size()} generated in ${t1-t0}ms"

        then:
        list.size() == 252
        list[0].values.size() == 4
        list[0].values['R1_REACTANT'] != null
        list[0].values['R1_INDEX'] != null
        list[0].values['R2_REACTANT'] != null
        list[0].values['R2_INDEX'] != null

    }
    
    void "chemaxon concurrent reactor"() {
        setup:
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
        //long t0 = System.currentTimeMillis()
        while ((products = crp.react()) != null) {
            for (Molecule product : products) {
                count++
            }
        }
        //long t1 = System.currentTimeMillis()
        //println "Number of products: $count generated in ${t1-t0}ms"
 
        then:
        count > 0;
        
        cleanup:
        importers.each { it.close() }
    }

    void "ignore options"() {

        println "ignore reactivity " + Reactor.IGNORE_REACTIVITY
        println "ignore slectivity " + Reactor.IGNORE_SELECTIVITY
        println "ignore tolerance " + Reactor.IGNORE_TOLERANCE


        when:
        int a = Reactor.IGNORE_REACTIVITY | Reactor.IGNORE_SELECTIVITY | Reactor.IGNORE_TOLERANCE
        int b = Reactor.IGNORE_REACTIVITY | Reactor.IGNORE_SELECTIVITY | Reactor.IGNORE_TOLERANCE | 0
        println "a " + a
        println "b " + b

        then:
        a == 7

    }

}

