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

package org.squonk.core.client

import org.squonk.data.Molecules
import org.squonk.io.DepictionParameters
import org.squonk.options.types.Structure

import java.util.zip.GZIPInputStream

import static org.squonk.io.DepictionParameters.OutputFormat.*
import spock.lang.Specification

/**
 * Created by timbo on 02/09/16.
 */
class StructureIOClientSpec extends Specification {

    static String baseurl = "http://localhost:8092/chem-services-cdk-basic/"

    static  StructureIOClient client =  new StructureIOClient.CDK() {
        @Override
        protected String getBase() {
             return baseurl
        }
    }

    void "simple get"() {
        URL url = new URL(baseurl + "moldepict?w=200&h=200&bg=%2300ffffff&expand=1&mol=C1%3DCC%3DCC%3DC1&molFormat=smiles&imgFormat=png")

        when:
        InputStream is = url.openStream()
        byte[] b = is.getBytes()

        then:
        b.length > 0
    }

    void "cdk smiles to png"() {

        when:
        byte[] img = client.renderImage("C1=CC=CC=C1", "smiles", png, new DepictionParameters(200, 200))

        then:
        img.length > 0
    }

    void "cdk mol to png"() {

        when:
        byte[] img = client.renderImage(Molecules.ethanol.v2000, "mol", png, new DepictionParameters(200, 200))

        then:
        img.length > 0
    }


    void "cdk smiles to svg"() {

        when:
        String svg = client.renderSVG("C1=CC=CC=C1", "smiles", new DepictionParameters(20, 20))

        then:
        svg.size() > 0
        svg.contains('<!DOCTYPE svg PUBLIC')
        svg.contains('Generated by the Chemistry Development Kit')
    }

    void "cdk mol to svg"() {

        when:
        String svg = client.renderSVG(Molecules.ethanol.v2000, "mol", new DepictionParameters(20, 20))

        then:
        svg.size() > 0
        svg.contains('<!DOCTYPE svg PUBLIC')
        svg.contains('Generated by the Chemistry Development Kit')
    }

    void "cdk export to sdf"() {

        when:
        String sdf = client.datasetExport(Molecules.nci10Dataset(), "sdf", false).text
        def parts = sdf.split('END')

        then:
        parts.length == 11
    }


    void "cdk export to gzipped sdf"() {

        when:
        def gzip = client.datasetExport(Molecules.nci10Dataset(), "sdf", true)
        String sdf = new GZIPInputStream(gzip).text
        def parts = sdf.split('END')

        then:
        parts.length == 11
    }


    void "cdk convert molecule formats"() {

        expect:
        Structure s = client.convertMol(source, format)
        s.source.length() > 0
        s.source.contains(result)
        s.format == format

        where:
        source                   | format   | result
        Molecules.ethanol.smiles | "mol"    | "V2000"
        Molecules.ethanol.smiles | "mol:v2" | "V2000"
        Molecules.ethanol.smiles | "mol:v3" | "V3000"
        Molecules.ethanol.v2000  | "smiles" | "CCO"
        Molecules.ethanol.v3000  | "smiles" | "CCO"
    }


    void "cdk multiple formats"() {

        when:
        Structure m = client.convertMol(Molecules.ethanol.smiles, "carrots", "mol", "beans")

        then:
        m.source.contains("V2000")
        m.format == "mol"
    }
}
