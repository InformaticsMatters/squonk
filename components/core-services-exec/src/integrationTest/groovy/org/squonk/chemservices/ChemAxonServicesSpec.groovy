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

package org.squonk.chemservices

import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.core.HttpServiceDescriptor
import org.squonk.data.Molecules
import org.squonk.dataset.Dataset
import org.squonk.types.io.JsonHandler
import org.squonk.util.CommonMimeTypes
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Collectors
import java.util.stream.Stream

class ChemAxonServicesSpec extends Specification {

    static String calculatorsbase = "//localhost:8092/chem-services-chemaxon-basic/rest/v1/calculators"
    static String descriptorsbase = "//localhost:8092/chem-services-chemaxon-basic/rest/v1/descriptors"
    static String B = CommonMimeTypes.MIME_TYPE_DATASET_BASIC_JSON
    static String M = CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON


    @Shared CamelContext context = new DefaultCamelContext()

    void setupSpec() {
        context.start()
    }

    void cleanup() {
        context?.stop()
    }


    void "fetch calculators ServiceDescriptors"() {

        when:
        InputStream json = new URL("http:"+calculatorsbase).openStream()
        Stream<HttpServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, HttpServiceDescriptor.class, false)

        then:
        sds.count() > 0

        cleanup:
        json?.close()

    }

    void "fetch descriptors ServiceDescriptors"() {

        when:
        InputStream json = new URL("http:"+descriptorsbase).openStream()
        Stream<HttpServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, HttpServiceDescriptor.class, false)

        then:
        sds.count() > 0

        cleanup:
        json?.close()

    }


    void "rest calculators json/json"() {

        Dataset dataset = Molecules.nci10Dataset()
        InputStream json = dataset.getInputStream(false)
        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        def headers = [
                (Exchange.HTTP_METHOD):"POST",
                "Content-Type":c,
                "Accept": a
        ]
        def output = pt.requestBodyAndHeaders("http4:"+calculatorsbase + path, json, headers)
        def stream = JsonHandler.instance.streamFromJson(output, cls, true)
        def list = stream.collect(Collectors.toList())
        list.size == 10
        list[0].values.size() == props

        cleanup:
        json.close()

        where:
        c | a | path                | cls                  | props
        M | B | '/logp'             | BasicObject.class    | 1
        M | M | '/logp'             | MoleculeObject.class | 1
        M | B | '/atomCount'        | BasicObject.class    | 1
        M | M | '/atomCount'        | MoleculeObject.class | 1
        M | B | '/lipinski'         | BasicObject.class    | 5 // 4 + error count
        M | M | '/lipinski'         | MoleculeObject.class | 5 // 4 + error count
    }

    void "test clustering json/json"() {

        Dataset dataset = Molecules.nci100Dataset()
        InputStream json = dataset.getInputStream(false)
        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        def headers = [
                (Exchange.HTTP_METHOD):"POST",
                "Content-Type":c,
                "Accept": a
        ]
        def output = pt.requestBodyAndHeaders("http4:"+descriptorsbase + path, json, headers)
        def stream = JsonHandler.instance.streamFromJson(output, cls, true)
        def list = stream.collect(Collectors.toList())
        list.size == count
        list[0].values.size() == props

        cleanup:
        json.close()

        where:
        c | a | path                        | cls                  | props | count
        M | B | '/clustering/spherex/ecfp4' | BasicObject.class    | 1     | 100
        M | M | '/clustering/spherex/ecfp4' | MoleculeObject.class | 1     | 100

    }

    // something broke in JChem resulting to XML parsing. Will be replaced with Dockerised version
    @Ignore
    void "test screening json/json"() {

        Dataset dataset = Molecules.nci100Dataset()
        InputStream json = dataset.getInputStream(false)
        ProducerTemplate pt = context.createProducerTemplate()

        expect:
        def headers = [
                (Exchange.HTTP_METHOD):"POST",
                "Content-Type":c,
                "Accept": a,
                "structure_source": 'CC1=CC(=O)C=CC1=O',
                "structure_format": 'smiles',
                "threshold": 0.4
        ]

        def output = pt.requestBodyAndHeaders("http4:"+descriptorsbase + path, json, headers)
        def stream = JsonHandler.instance.streamFromJson(output, cls, true)
        def list = stream.collect(Collectors.toList())
        list.size >=1 && list.size < 100
        list[0].values.size() == 1

        cleanup:
        json.close()

        where:
        c | a | path                       | cls
        M | B | '/screening/ecfp4'         | BasicObject.class
        M | M | '/screening/ecfp4'         | MoleculeObject.class
        M | B | '/screening/pharmacophore' | BasicObject.class
        M | M | '/screening/pharmacophore' | MoleculeObject.class

    }


}