package org.squonk.types

import com.sun.mail.util.BASE64DecoderStream
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.types.io.JsonHandler
import spock.lang.Shared
import spock.lang.Specification

import javax.activation.DataHandler
import javax.activation.DataSource
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Created by timbo on 18/10/2016.
 */
class GenericModelHandlerSpec extends Specification {


    @Shared
    CamelContext context = new DefaultCamelContext()

    void setupSpec() {
        context.addRoutes(new RouteBuilder() {


            @Override
            void configure() throws Exception {

                from("direct:foo")
                //.log('foo attachments: ${exchange.in.attachmentNames.size()}')
                        .process() { exch ->
                    println "foo attachments: " + exch.in.attachmentNames.size()

                }
                // String multipartSubType, boolean multipartWithoutAttachment, boolean headersInline, String includeHeaders, boolean binaryContent
                .marshal().mimeMultipart("mixed", false, false, null, true)
                .setHeader("Content-Encoding", constant("gzip"))
                .marshal().gzip()
                .to('jetty:http://localhost:8889/bar')

                from('jetty:http://localhost:8889/bar')
                        .process() { exch ->
                    exch.in.headers.each { k,v ->
                        println "header: $k $v"
                    }
                    println "exch: " + exch.in.body
                    exch.in.body = exch.in.body.text
                    println exch.in.body
                    println "http attachments: " + exch.in.attachmentNames.size()
                    //println exch.in.getAttachment('var1').inputStream.text
                }
                .unmarshal().mimeMultipart()
                .to("mock:baz")

            }
        })

        context.start()
    }

    void shutdownSpec() {
        context.stop()
    }

    void "as attachments"() {

        GenericModel h = new GenericModel(new BigInteger(100), ['var1', 'var2'] as String[])
        h.setStream('var1', new ByteArrayInputStream("Hello World!".bytes))
        h.setStream('var2', new ByteArrayInputStream("Goodbye Mars!".bytes))

        def pt = context.createProducerTemplate()

        when:
        def resultEndpoint = context.getEndpoint('mock:baz')
        resultEndpoint.expectedMessageCount(1)
        pt.send("direct:foo", new Processor() {
            @Override
            void process(Exchange exch) throws Exception {
                exch.in.setHeader(Exchange.HTTP_METHOD, "POST")
                exch.in.setHeader(Exchange.CONTENT_TYPE, "application/json")
                exch.in.setBody(JsonHandler.instance.objectToJson(h))
                println "setting body"
                for (String name : h.streamNames) {
                    println "setting attachment $name"
                    try {
                        DataSource ds = new ModelStreamDataSource(name, h.getStream(name).getBytes())
                        exch.in.addAttachment(name, new DataHandler(ds))
                    } catch (Exception ex) {
                        ex.printStackTrace()
                    }
                }
                println 'done'
            }

        })


        then:
        def msg = resultEndpoint.receivedExchanges.in[0]
        msg != null
        //println 'body: ' + msg.body.text
        println "unmarshalled attachements: " + msg.attachmentNames.size()
        msg.attachmentNames.size() == 2
        println 'attachment 0: ' + msg.getAttachment(msg.attachmentNames[0]).inputStream.text
    }

    InputStream gzip(String s) {
        println "String bytes: " + s.bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        GZIPOutputStream gzip = new GZIPOutputStream(out)
        gzip.write(s.bytes)
        gzip.close()
        return new ByteArrayInputStream(out.toByteArray())
    }

    String gunzip(InputStream is) {
        def gzip =  new GZIPInputStream(is)
        return gzip.text
    }

    class ModelStreamDataSource implements DataSource {

        private String name;
        private byte[] bytes

        ModelStreamDataSource(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        @Override
        InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(bytes)
        }

        @Override
        OutputStream getOutputStream() throws IOException {
            //return new ByteArrayOutputStream(bytes)
            return null
        }

        @Override
        String getContentType() {
            return "application/binary";
        }

        @Override
        String getName() {
            return name;
        }
    }

}
