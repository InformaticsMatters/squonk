package org.squonk.cdk.io

import org.openscience.cdk.ChemFile
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.io.FormatFactory
import org.openscience.cdk.io.ISimpleChemObjectReader
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.io.MDLV3000Reader
import org.openscience.cdk.io.SMILESReader
import org.openscience.cdk.io.formats.IChemFormat
import org.openscience.cdk.silent.AtomContainer
import org.openscience.cdk.tools.manipulator.ChemFileManipulator
import spock.lang.Specification

import java.awt.Color
import java.awt.Dimension

/**
 * Created by timbo on 17/01/2016.
 */
class MolDepictSpec extends Specification {

    String caffeine = 'CN1C=NC2=C1C(=O)N(C)C(=O)N2C'


    void "smiles to svg"() {

        //DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, new Color(255, 255, 255, 0))
        MolDepict d = new MolDepict(params)

        when:
        String svg1 = d.smilesToSVG(CDKMoleculeIOUtilsSpec.smiles)
        //println svg1

        then:
        svg1 != null
    }

    void "v2000 to svg"() {

        DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        MolDepict d = new MolDepict(params)

        when:
        String svg1 = d.v2000ToSVG(CDKMoleculeIOUtilsSpec.v2000)
        //println svg1

        then:
        svg1 != null
    }

    void "v3000 to svg"() {

        DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        MolDepict d = new MolDepict(params)

        when:
        String svg1 = d.v3000ToSVG(CDKMoleculeIOUtilsSpec.v3000)
        //println svg1

        then:
        svg1 != null
    }

    void "guess smiles to svg"() {

        DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        MolDepict d = new MolDepict(params)

        when:
        String svg = d.moleculeToSVG(CDKMoleculeIOUtilsSpec.smiles)
        println svg

        then:
        svg != null

    }

    void "guess v2000 to svg"() {

        DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        MolDepict d = new MolDepict(params)

        when:
        String svg = d.moleculeToSVG(CDKMoleculeIOUtilsSpec.v2000)
        println svg

        then:
        svg != null

    }

    void "guess v3000 to svg"() {

        DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        MolDepict d = new MolDepict(params)

        when:
        String svg = d.moleculeToSVG(CDKMoleculeIOUtilsSpec.v3000)
        println svg

        then:
        svg != null

    }


}
