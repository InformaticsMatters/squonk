/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.squonk.cdk.molecule

import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.silent.Atom
import org.openscience.cdk.silent.AtomContainer
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class CDKMoleculeUtilsSpec extends Specification {
    
    void "add implicit H"() {
        IAtomContainer methane = new AtomContainer()
        IAtom carbon = new Atom("C")
        methane.addAtom(carbon)
        
               
        when:
        CDKMoleculeUtils.initializeMolecule(methane)
        
        then:
        methane.atomCount == 1
        carbon.implicitHydrogenCount == 4
        
        
    }
	
}

