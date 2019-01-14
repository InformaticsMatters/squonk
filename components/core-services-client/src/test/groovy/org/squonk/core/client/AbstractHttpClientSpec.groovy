package org.squonk.core.client

import groovy.json.JsonSlurper
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.InputStreamEntity
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import spock.lang.Specification

import java.util.zip.GZIPInputStream

class AbstractHttpClientSpec extends Specification {

    void "post streaming simple"() {

        String text = 'Hello world!'
        def client = new AbstractHttpClient()

        when:
        InputStream is = client.executePostAsInputStreamStreaming(
                new URIBuilder("https://postman-echo.com/post"),
                new StringEntity(text),
                new BasicNameValuePair("Content-Type", "text/plain")
        )
        def jsonSlurper = new JsonSlurper()
        def map = jsonSlurper.parse(is)

        then:
        map.data == text

        cleanup:
        client.close()
    }

    void "post streaming large"() {

        def client = new AbstractHttpClient()
        def input = ""
        (1..1000).each {
            input += it
            input += " dfgdlksfgjjlkdfssjasfhafherihfkervnerhulhervbvhejrhbjkenilerjrenerlkjfernrjk\n"
        }
        //println input

        when:
        InputStream is = client.executePostAsInputStreamStreaming(
                new URIBuilder("https://postman-echo.com/post"),
                new StringEntity(input),
                new BasicNameValuePair("Content-Type", "text/plain")
                //null
        )
        def text = is.text
        //println text
        def jsonSlurper = new JsonSlurper()
        def map = jsonSlurper.parse(text.bytes)
        //println map.data

        then:
        map.data.split("\n").length == 1000

        cleanup:
        client.close()
    }

}
