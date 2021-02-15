package org.squonk.core.client

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.http.client.utils.URIBuilder
import org.squonk.camel.testsupport.CamelSpecificationBase

import javax.ws.rs.core.UriBuilder
import java.util.zip.GZIPOutputStream

class AsyncHttpClientSpec extends CamelSpecificationBase {


    def 'simple test'() {

        def client = new AbstractAsyncHttpClient()


        when:
        URIBuilder uri = new URIBuilder()
                .setScheme('http')
                .setHost('localhost:8888')
                .setPath('data')
        AbstractAsyncHttpClient.AsyncResponse resp = client.executeGet(uri, null)
        def http = resp.httpResponse.get()
        println http.getStatusLine()
        http.getAllHeaders().each {
            println "Header $it.name -> $it.value"
        }

        def is = resp.inputStream
        byte[] buf = new byte[1024]
        while (true) {
            int size = is.read(buf)
            if (size < 0) {
                println "No more data"
                break
            }
            println "Read $size"
        }
        is.close()

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
                        GZIPOutputStream gzip = new GZIPOutputStream(pout)
                        OutputStream out = gzip
                        //OutputStream out = pout
                        exchange.getIn().setBody(pin)
                        Thread t = new Thread() {
                            @Override
                            void run() {
                                (1..1000).each {
                                    println "writing $it"
                                    out.write("$it A very very long string indeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeed\n".bytes)
                                    out.flush()
                                    sleep(1)
                                }
                                out.close()
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
