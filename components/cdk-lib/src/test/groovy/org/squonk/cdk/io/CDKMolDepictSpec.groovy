package org.squonk.cdk.io

import spock.lang.Specification

import java.awt.Color

/**
 * Created by timbo on 17/01/2016.
 */
class CDKMolDepictSpec extends Specification {

    void "smiles to svg"() {

        //DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        DepictionParameters params = new DepictionParameters(40, 30, true, new Color(255, 255, 255, 0))
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.smilesToSVG(CDKMoleculeIOUtilsSpec.smiles)
        //println svg1

        then:
        svg1 != null
    }

    void "smiles to svg default"() {

        CDKMolDepict d = new CDKMolDepict()

        when:
        String svg1 = d.smilesToSVG(CDKMoleculeIOUtilsSpec.smiles)
        //println svg1

        then:
        svg1 != null
    }

    void "smiles to svg override"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict()

        when:
        String svg1 = d.smilesToSVG(CDKMoleculeIOUtilsSpec.smiles, params)
        //println svg1

        then:
        svg1 != null
    }


    void "v2000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.v2000ToSVG(CDKMoleculeIOUtilsSpec.v2000)
        //println svg1

        then:
        svg1 != null
    }

    void "v3000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.v3000ToSVG(CDKMoleculeIOUtilsSpec.v3000)
        //println svg1

        then:
        svg1 != null
    }

    void "guess smiles to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg = d.stringToSVG(CDKMoleculeIOUtilsSpec.smiles)
        //println svg

        then:
        svg != null

    }

    void "guess v2000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg = d.stringToSVG(CDKMoleculeIOUtilsSpec.v2000)
        //println svg

        then:
        svg != null

    }

    void "guess v3000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg = d.stringToSVG(CDKMoleculeIOUtilsSpec.v3000)
        //println svg

        then:
        svg != null

    }

    void "smiles to png"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        byte[] png = d.smilesToImage(CDKMoleculeIOUtilsSpec.smiles, 'PNG')

        then:
        png != null
        png.length > 0

    }

}
