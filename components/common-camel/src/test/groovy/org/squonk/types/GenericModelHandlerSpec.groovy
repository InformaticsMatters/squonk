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

package org.squonk.types

import com.sun.xml.internal.org.jvnet.mimepull.MIMEConfig
import com.sun.xml.internal.org.jvnet.mimepull.MIMEMessage
import com.sun.xml.internal.org.jvnet.mimepull.MIMEPart
import org.apache.camel.Attachment
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultAttachment
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.types.io.JsonHandler
import spock.lang.Shared
import spock.lang.Specification

import javax.activation.DataSource
import java.util.regex.Matcher
import java.util.regex.Pattern
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
                    exch.in.headers.each { k, v ->
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

                from("direct:mimepull")
                        .process() { exch ->
                    println "mimepull attachments: " + exch.in.attachmentNames.size()
                }
                .marshal().mimeMultipart("mixed", false, false, null, true)
                //.setHeader("Content-Encoding", constant("gzip"))
                //.marshal().gzip()
                        .to("mock:mimepull")

            }
        })

        context.start()
    }

    void shutdownSpec() {
        context.stop()
    }

    void "as attachments"() {

        GenericModel h = new GenericModel(new BigInteger(100), ['attachment1', 'attachment2'] as String[])
        h.setStream('attachment1', new ByteArrayInputStream("Hello World!".bytes))
        h.setStream('attachment2', new ByteArrayInputStream("Goodbye Mars!".bytes))

        def pt = context.createProducerTemplate()

        when:
        def resultEndpoint = context.getEndpoint('mock:baz')
        resultEndpoint.expectedMessageCount(1)
        pt.send("direct:foo", new ModelProcessor(h))

        then:
        def msg = resultEndpoint.receivedExchanges.in[0]
        msg != null
        //println 'body: ' + msg.body.text
        println "unmarshalled attachments: " + msg.attachmentNames.size()
        msg.attachmentNames.size() == 2
        println 'attachment 0: ' + msg.getAttachment(msg.attachmentNames[0]).inputStream.text
    }

    void "as mimepull"() {

        GenericModel h = new GenericModel(new BigInteger(100), ['attachment1', 'attachment2'] as String[])
        h.setStream('attachment1', new ByteArrayInputStream("Hello World!".bytes))
        h.setStream('attachment2', new ByteArrayInputStream("Goodbye Mars!".bytes))

        def pt = context.createProducerTemplate()

        when:
        def resultEndpoint = context.getEndpoint('mock:mimepull')
        resultEndpoint.expectedMessageCount(1)
        pt.send("direct:mimepull", new ModelProcessor(h))

        then:
        def msg = resultEndpoint.receivedExchanges.in[0]
        msg != null
        InputStream is = msg.getBody(InputStream.class)
        String body = is.text
        println '======================\n' + body + '\n======================\n'
        String contentType = msg.getHeader("Content-Type")
        //println "Content-Type: " + contentType
        Pattern p = Pattern.compile("\\s+boundary=\"(.*)\"")
        Matcher m = p.matcher(contentType)
        String boundary
        if (m.find()) {
            boundary = m.group(1)
            //println "boundary: " + boundary
        } else {
            throw new IllegalStateException("No boundary found")
        }

        MIMEConfig config = new MIMEConfig()
        MIMEMessage mm = new MIMEMessage(new ByteArrayInputStream(body.getBytes()), boundary, config);

        MIMEPart part0 = mm.getPart(0);
        println part0
        println part0.getContentId()
        InputStream ais0 = part0.readOnce();
        println "part0: " + ais0.text

        MIMEPart part1 = mm.getPart("attachment1");
        println part1
        println part1.getContentId()
        InputStream ais1 = part1.readOnce();
        println "part1: " + ais1.text
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
        def gzip = new GZIPInputStream(is)
        return gzip.text
    }

    class ModelProcessor implements Processor {
        GenericModel model

        ModelProcessor(GenericModel model) {
            this.model = model;
        }

        @Override
        void process(Exchange exch) throws Exception {
            println "processing"
            exch.in.setHeader(Exchange.HTTP_METHOD, "POST")
            exch.in.setHeader(Exchange.CONTENT_TYPE, "application/json")
            exch.in.setBody(JsonHandler.instance.objectToJson(model))
            println "setting body"
            for (String name : model.streamNames) {
                println "setting attachment $name"
                try {
                    DataSource ds = new ModelStreamDataSource(name, model.getStream(name).getBytes())
                    Attachment attach = new DefaultAttachment(ds)
                    attach.setHeader("Content-Id", name)
                    exch.in.addAttachmentObject(name, attach)
                } catch (Exception ex) {
                    ex.printStackTrace()
                }
            }
            println 'done'
        }
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
