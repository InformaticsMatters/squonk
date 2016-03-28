package org.squonk.http

import com.im.lac.types.BasicObject
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.http4.HttpMethods
import org.apache.camel.impl.DefaultCamelContext
import org.squonk.dataset.Dataset
import org.squonk.dataset.DatasetMetadata
import org.squonk.types.DatasetHandler
import org.squonk.types.StringHandler
import org.squonk.util.IOUtils
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by timbo on 23/03/2016.
 */
class DefaultHttpContextSpec extends Specification {

    @Shared
    DefaultCamelContext context

    void setupSpec() {
        context = new DefaultCamelContext()
        context.addRoutes(new RouteBuilder() {

            @Override
            void configure() throws Exception {

                from("jetty:http://localhost:8889/sayhello")
                        .setBody(constant("hello world"))

                from("jetty:http://localhost:8889/echo")
                    .process() { Exchange exch ->
                        InputStream is = exch.getIn().getBody(InputStream.class)
                        String s = IOUtils.convertStreamToString(is)
                        exch.getOut().setBody(s)
                }
            }
        })


        context.start()
    }

    void cleanupSpec() {
        context?.shutdown()
    }

    void "simple get"() {

        CamelRequestResponseExecutor hc = new CamelRequestResponseExecutor(context, HttpMethods.GET,
                URI.create("http://localhost:8889/sayhello"))
        StringHandler sh = new StringHandler()

        when:
        sh.prepareRequest(null, hc, false)
        hc.execute()

        then:
        sh.readResponse(hc, false) == "hello world"
    }

    void "basic objects post"() {

        CamelRequestResponseExecutor exec = new CamelRequestResponseExecutor(context, HttpMethods.POST,
                URI.create("http://localhost:8889/echo"))
        DatasetHandler dh = new DatasetHandler(BasicObject.class)

        def input = [
                new BasicObject([name: 'venus']),
                new BasicObject([name: 'mercury']),
                new BasicObject([name: 'earth']),
        ]
        Dataset ds = new Dataset(BasicObject.class, input,
                new DatasetMetadata(BasicObject.class, [name: String.class], 3))

        when:
        dh.prepareRequest(ds, exec, false)
        exec.execute()
        Dataset output = dh.readResponse(exec, false)


        then:
        output.items.size() == 3
    }

    void "send basic receive string"() {

        CamelRequestResponseExecutor exec = new CamelRequestResponseExecutor(context, HttpMethods.POST,
                URI.create("http://localhost:8889/echo"))
        DatasetHandler dh = new DatasetHandler(BasicObject.class)
        StringHandler sh = new StringHandler()

        def input = [
                new BasicObject([name: 'venus']),
                new BasicObject([name: 'mercury']),
                new BasicObject([name: 'earth']),
        ]
        Dataset ds = new Dataset(BasicObject.class, input,
                new DatasetMetadata(BasicObject.class, [name: String.class], 3))

        when:
        dh.prepareRequest(ds, exec, false)
        exec.execute()
        String output = sh.readResponse(exec, false)

        then:
        output.length() > 0
    }

    void "send string receive basic"() {

        CamelRequestResponseExecutor exec = new CamelRequestResponseExecutor(context, HttpMethods.POST,
                URI.create("http://localhost:8889/echo"))
        DatasetHandler dh = new DatasetHandler(BasicObject.class)
        StringHandler sh = new StringHandler()

        def input = '[{"uuid":"f91972fd-7087-4437-abfb-c2c2eb5eb89f","values":{"name":"venus"}},{"uuid":"af245c3f-b87c-4c64-b0af-0df99fe86427","values":{"name":"mercury"}},{"uuid":"bd755fef-b4ae-44e5-b224-ceb39be66708","values":{"name":"earth"}}]'

        when:
        sh.prepareRequest(input, exec, false)
        exec.execute()
        Dataset output = dh.readResponse(exec, false)


        then:
        output.items.size() == 3
    }


}
