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

package org.squonk.camel.processor

import org.squonk.io.SquonkDataSource
import org.squonk.io.StringDataSource
import org.squonk.types.MoleculeObject
import org.apache.camel.Exchange
import org.apache.camel.ProducerTemplate
import org.apache.camel.TypeConversionException
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.support.TypeConverterSupport
import org.squonk.api.MimeTypeResolver
import org.squonk.dataset.Dataset
import org.squonk.dataset.MoleculeObjectDataset
import org.squonk.types.SDFile
import org.squonk.types.TypeResolver
import spock.lang.Shared
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 27/03/2016.
 */
class MoleculeObjectRouteHttpProcessorSpec extends Specification {

    @Shared
    DefaultCamelContext context

    @Shared
    TypeResolver typeResolver = new TypeResolver()

    void setupSpec() {
        context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {

                from("direct:simple").process() { Exchange exch ->
                    MoleculeObjectDataset ds = exch.getIn().getBody(MoleculeObjectDataset.class)
                    Stream st = ds.dataset.stream.peek() {
                        it.putValue("hello", "world")
                    }
                    exch.getIn().setBody(new MoleculeObjectDataset(st))
                }

                from("direct:poster")
                     .process(new MoleculeObjectRouteHttpProcessor("direct:simple", typeResolver))

            }
        })

        context.getTypeConverterRegistry().addTypeConverter(SDFile.class, MoleculeObjectDataset.class, new TypeConverterSupport() {

            @Override
            SDFile convertTo(Class type, Exchange exch, Object value) throws TypeConversionException {
                def ds = new StringDataSource(SquonkDataSource.ROLE_DEFAULT, null, null, "Hello world!", false)
                return new SDFile(ds)
            }
        })

        context.start()
    }

    void cleanupSpec() {
        context?.shutdown()
    }

    @Shared def mols = [
            new MoleculeObject('CC1=CC(=O)C=CC1=O', 'smiles', [fruit: 'apple', index: 1]),
            new MoleculeObject('S(SC1=NC2=CC=CC=C2S1)', 'smiles', [fruit: 'orange', index: 2]),
            new MoleculeObject('CC(=O)OC(CC([O-])=O)C[N+](C)(C)C', 'smiles', [fruit: 'pear', index: 3]),
            new MoleculeObject('[O-][N+](=O)C1=CC(=C(Cl)C=C1)[N+]([O-])=O', 'smiles', [fruit: 'banana', index: 4]),
            new MoleculeObject('OC1C(O)C(O)C(OP(O)(O)=O)C(O)C1O', 'smiles', [fruit: 'melon', index: 5])
    ]

    void "mo to mo"() {
        ProducerTemplate pt = context.createProducerTemplate()
        Map headers = [:]
        Dataset<MoleculeObject> ds = new Dataset<>(MoleculeObject.class, mols)

        when:
        def results = pt.requestBodyAndHeaders("direct:poster", ds.getInputStream(false), headers)
        String s = results.text

        then:
        results != null
        s.split('hello').size() == 6
        s.split('fruit').size() == 6
    }

    void "mo to bo"() {
        ProducerTemplate pt = context.createProducerTemplate()
        Map headers = ['Accept':MimeTypeResolver.MIME_TYPE_DATASET_BASIC_JSON]
        Dataset<MoleculeObject> ds = new Dataset<>(MoleculeObject.class, mols)

        when:
        def results = pt.requestBodyAndHeaders("direct:poster", ds.getInputStream(false), headers)
        String s = results.text

        then:
        results != null
        s.split('hello').size() == 6
        s.split('fruit').size() == 1 // should be no fruit
    }

    // No real SDFile type converters will be present in this module as they are vendor dependent.
    // Instead we test out mock one we added to the context for this purpose.
    void "mo to sdf"() {
        ProducerTemplate pt = context.createProducerTemplate()
        Map headers = ['Accept':MimeTypeResolver.MIME_TYPE_MDL_SDF]
        Dataset<MoleculeObject> ds = new Dataset<>(MoleculeObject.class, mols)

        when:
        def results = pt.requestBodyAndHeaders("direct:poster", ds.getInputStream(false), headers)
        String s = results.text

        then:
        results != null
        s == "Hello world!"
    }

}
