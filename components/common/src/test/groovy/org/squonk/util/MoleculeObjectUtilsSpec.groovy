package org.squonk.util

import com.im.lac.types.MoleculeObject
import spock.lang.Specification

import java.util.stream.Collectors

/**
 * Created by timbo on 09/05/2016.
 */
class MoleculeObjectUtilsSpec extends Specification {

    void "two different mols"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC']),
                new MoleculeObject('CCC', 'smiles', [canon: 'CCC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', [], [], []).collect(Collectors.toList())

        then:
        results.size() == 2
        results[0].values.size() == 0
        results[1].values.size() == 0
    }

    void "two identical mols"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', [], [], []).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 0
    }

    void "keep first, last, append, lose"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'first', b: 'first', c: 1, d: 'random']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'second', b: 'second', c: 2, d: 'noise'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 3
        results[0].getValue('a') == 'first'
        results[0].getValue('b') == 'second'
        results[0].getValue('c') instanceof List
        results[0].getValue('c').size() == 2
    }

    void "first null"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'second', b: 'second', c: 2, d: 'noise'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 3
        results[0].getValue('a') == 'second'
        results[0].getValue('b') == 'second'
        results[0].getValue('c') instanceof List
        results[0].getValue('c').size() == 1
    }

    void "second null"() {
        def mols = [
                new MoleculeObject('CC', 'smiles', [canon: 'CC', a: 'first', b: 'first', c: 1, d: 'random']),
                new MoleculeObject('CC', 'smiles', [canon: 'CC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].values.size() == 3
        results[0].getValue('a') == 'first'
        results[0].getValue('b') == 'first'
        results[0].getValue('c') instanceof List
        results[0].getValue('c').size() == 1
    }

    void "structure replaced"() {
        def mols = [
                new MoleculeObject('XX', 'smiles', [canon: 'CC', a: 'first', b: 'first', c: 1, d: 'random']),
                new MoleculeObject('XX', 'smiles', [canon: 'CC'])
        ]

        when:
        def results =  MoleculeObjectUtils.deduplicate(mols.stream(), 'canon', ['a'], ['b'], ['c']).collect(Collectors.toList())

        then:
        results.size() == 1
        results[0].source == 'CC'
    }


}
