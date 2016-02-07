package org.squonk.cdk.io

import org.squonk.data.Molecules
import org.squonk.io.DepictionParameters
import spock.lang.Specification

import java.awt.Color

/**
 * Created by timbo on 17/01/2016.
 */
class CDKMolDepictSpec extends Specification {


    void f() {

        when:
        //int i = Integer.parseInt('FFFFFF', 16)
        //println i
        //Color col = new Color(i, true)

        println Integer.toHexString(Integer.MAX_VALUE);

        Color col = new Color(Long.decode('#EEFFFFFF').intValue(), true);
        println Integer.toHexString(col.getRGB());
        println Integer.toHexString(col.getAlpha());
        println col.toString()

        then:
        col != null



    }

    void "smiles to svg"() {

        //DepictionParameters params = new DepictionParameters(new Dimension(40, 30), true, Color.YELLOW)
        DepictionParameters params = new DepictionParameters(40, 30, true, new Color(255, 255, 255, 0))
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.smilesToSVG(Molecules.ethanol.smiles)
        //println svg1

        then:
        svg1 != null
    }

    void "smiles to svg default"() {

        CDKMolDepict d = new CDKMolDepict()

        when:
        String svg1 = d.smilesToSVG(Molecules.ethanol.smiles)
        //println svg1

        then:
        svg1 != null
    }

    void "smiles to svg override"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict()

        when:
        String svg1 = d.smilesToSVG(Molecules.ethanol.smiles, params)
        //println svg1

        then:
        svg1 != null
    }


    void "v2000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.v2000ToSVG(Molecules.ethanol.v2000)
        //println svg1

        then:
        svg1 != null
    }

    void "v3000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.v3000ToSVG(Molecules.ethanol.v3000)
        //println svg1

        then:
        svg1 != null
    }

    void "guess smiles to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg = d.stringToSVG(Molecules.ethanol.smiles)
        //println svg

        then:
        svg != null

    }

    void "guess v2000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg = d.stringToSVG(Molecules.ethanol.v2000)
        //println svg

        then:
        svg != null

    }

    void "guess v3000 to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, Color.YELLOW)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg = d.stringToSVG(Molecules.ethanol.v3000)
        //println svg

        then:
        svg != null

    }

    void "smiles to png"() {

        DepictionParameters params = new DepictionParameters(200, 200, true, Color.WHITE)
        CDKMolDepict d = new CDKMolDepict(params)

        when:


        //byte[] png = d.smilesToImage("CN1C=NC2=C1C(=O)N(C)C(=O)N2C", 'PNG')

        byte[] png = d.smilesToImage("[H][#7]-[#6]-1=[#6]-[#6]=[#6]-[#6]=[#6]-1", 'PNG')

        //byte[] png = d.smilesToImage("[H]C1C(N)N(C)C2=C(N(C)C=N2)C1=O", 'PNG')
        //byte[] png = d.smilesToImage(Molecules.ethanol.smiles, 'PNG')
//        FileOutputStream f = new FileOutputStream("/users/timbo/tmp/test_cdk.png")
//        f.write(png)
//        f.flush()
//        f.close()

        then:
        png != null
        png.length > 0

    }

}
