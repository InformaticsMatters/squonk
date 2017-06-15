/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.cdk.io

import org.openscience.cdk.depict.DepictionGenerator
import org.openscience.cdk.renderer.color.CDK2DAtomColors
import org.openscience.cdk.renderer.color.CPKAtomColors
import org.openscience.cdk.renderer.color.UniColor
import org.squonk.data.Molecules
import org.squonk.io.DepictionParameters
import org.squonk.io.DepictionParameters.HighlightMode
import org.squonk.io.DepictionParameters.ColorScheme
import spock.lang.Specification

import java.awt.Color

/**
 * Created by timbo on 17/01/2016.
 */
class CDKMolDepictSpec extends Specification {

//    void "dg test"() {
//
//        DepictionGenerator dg = new DepictionGenerator()
//        dg = dg.withAtomColors(new CPKAtomColors())
//        CDKMolDepict md = new CDKMolDepict()
//        def mol = md.smilesToMolecule(Molecules.caffeine.smiles)
//
//        when:
//        def depiction = dg.depict(mol)
//        println depiction.toSvgStr()
//
//        then:
//        1 == 1
//    }


    void f() {

        when:

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
        DepictionParameters params = new DepictionParameters(40, 30, true, new Color(255, 255, 255, 0), ColorScheme.toolkit_default)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.smilesToSVG(Molecules.ethanol.smiles)
        println svg1

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

    void "smiles to svg highlight"() {

        DepictionParameters params = new DepictionParameters(200, 150, true, new Color(255, 255, 255, 0), ColorScheme.black)
                .addAtomHighlight([0,1,2] as int[], Color.cyan, HighlightMode.region, true)
                .addAtomHighlight([5,6,7] as int[], Color.orange, HighlightMode.region, true)
        params.setMargin(5d)
        CDKMolDepict d = new CDKMolDepict(params)

        when:
        String svg1 = d.smilesToSVG(Molecules.caffeine.smiles)
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
