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
