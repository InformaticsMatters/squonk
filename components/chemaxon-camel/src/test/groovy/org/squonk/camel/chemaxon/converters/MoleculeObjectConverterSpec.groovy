package org.squonk.camel.chemaxon.converters

import org.apache.camel.CamelContext
import org.apache.camel.ProducerTemplate
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Shared
import spock.lang.Specification
import org.squonk.types.MoleculeObjectIterable
import java.util.zip.GZIPInputStream

/**
 *
 * @author timbo
 */
class MoleculeObjectConverterSpec extends Specification {

    @Shared CamelContext camelContext
    @Shared smiles10 = """\
C
CC
CCC
CCCC
CCCCC
CCCCCC
CCCCCCC
CCCCCCCC
CCCCCCCCC
CCCCCCCCCC"""

    def setupSpec() { // run before the first feature method
        camelContext = new DefaultCamelContext()
        camelContext.addRoutes(new RouteBuilder() {
                public void configure() {
                    from("direct:simple")
                    .convertBodyTo(MoleculeObjectIterable.class)
                    .to('mock:result')

                }
            })
        camelContext.start()
    }


    def cleanupSpec() { // run after the last feature method
        camelContext.stop()
    }

    def "InputStream to MolecuelObjectIterable"() {
        setup:
        def is = new ByteArrayInputStream(smiles10.getBytes())

        when:
        def converted = camelContext.getTypeConverter().convertTo(MoleculeObjectIterable.class, is)

        then:
        converted != null
        converted instanceof MoleculeObjectIterable
    }

    def "gzip convert"() {
        setup:
        ProducerTemplate t = camelContext.createProducerTemplate()
        GZIPInputStream gzip = new GZIPInputStream(new FileInputStream("../../data/testfiles/dhfr_standardized.sdf.gz"))

        when:
        def result = t.requestBody("direct:simple", gzip)
        int count = 0
        result.each {
            count++
        }
        
        then:
        result instanceof MoleculeObjectIterable
        count == 756
        
        cleanup:
        result.close()

    }



}