package org.squonk.core.client

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.http.client.utils.URIBuilder
import org.squonk.camel.testsupport.CamelSpecificationBase

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class AsyncHttpClientSpec extends CamelSpecificationBase {


    def 'simple test'() {

        def client = new AbstractAsyncHttpClient()


        when:
        def url = "http://localhost:8888/data"
        AbstractAsyncHttpClient.AsyncResponse resp = client.executeGet(new URIBuilder().setPath(url), null)
        println resp.httpResponse.get().getStatusLine()
        //GZIPInputStream gzip = new GZIPInputStream(resp.inputStream)
        BufferedReader reader = new BufferedReader(new InputStreamReader(resp.inputStream))
//        reader.eachLine {
//            println "OUTPUT $it"
//        }


        then:
        resp != null
    }

    @Override
    void addRoutes(CamelContext context) {
        context.addRoutes(new RouteBuilder() {
            @Override
            void configure() throws Exception {

                from("jetty:http://0.0.0.0:8888/data")
                .log("Received request")
                .process(new Processor() {

                    @Override
                    void process(Exchange exchange) throws Exception {
                        PipedInputStream pin = new PipedInputStream()
                        PipedOutputStream pout = new PipedOutputStream(pin)
                        //GZIPOutputStream gzip = new GZIPOutputStream(pout)
                        exchange.getIn().setBody(pin)
                        Thread t = new Thread() {
                            @Override
                            void run() {
                                (1..100).each {
                                    //println "writing $it"
                                    pout.write("$it A very very long string indeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeed\n".bytes)
                                    pout
                                    sleep(100)
                                }
                                pout.close()
                                println "closed"
                            }
                        }
                        t.start()
                    }
                })
            }
        })
    }
}
