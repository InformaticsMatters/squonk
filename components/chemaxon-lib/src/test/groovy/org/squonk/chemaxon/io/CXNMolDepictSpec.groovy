package org.squonk.chemaxon.io

import org.squonk.data.Molecules
import org.squonk.io.DepictionParameters
import spock.lang.Specification

import java.awt.Color

/**
 * Created by timbo on 25/01/2016.
 */
class CXNMolDepictSpec extends Specification {


    void "smiles to svg"() {

        DepictionParameters params = new DepictionParameters(40, 30, true, new Color(255, 255, 255, 0))
        CXNMolDepict d = new CXNMolDepict(params)

        when:
        String svg1 = d.smilesToSVG(Molecules.ethanol.smiles)
        //println svg1

        then:
        svg1 != null
    }

    void "smiles to png"() {

        DepictionParameters params = new DepictionParameters(100, 100, true, new Color(255, 255, 0, 50))
        CXNMolDepict d = new CXNMolDepict(params)

        when:
        byte[] png = d.smilesToImage("CN1C=NC2=C1C(=O)N(C)C(=O)N2C", "PNG")
        //println png
//        FileOutputStream f = new FileOutputStream("/users/timbo/tmp/caffeine_cxn.png")
//        f.write(png)
//        f.flush()
//        f.close()


        then:
        png != null
    }

}
