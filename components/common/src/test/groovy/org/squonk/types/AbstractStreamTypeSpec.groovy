package org.squonk.types

import spock.lang.Specification

class AbstractStreamTypeSpec extends Specification {

    static String hello = "Hello World!"
    static String goodbye = "Goodbye World!"


    void "one stream tests"() {

        when:
        def bais = new ByteArrayInputStream(hello.getBytes())
        def simple = new SimpleAbstractStreamType(bais)

        then:
        simple.getInputStreams().length == 1
        simple.getInputStream() == bais
        simple.getGzippedInputStreams().length == 1
        simple.getGunzippedInputStreams().length == 1
        isGzipped(simple.getGzippedInputStream())
        !isGzipped(simple.getGunzipedInputStream())
    }

    void "two stream tests"() {

        when:
        def bais0 = new ByteArrayInputStream(hello.getBytes())
        def bais1 = new ByteArrayInputStream(goodbye.getBytes())
        def simple = new SimpleAbstractStreamType([bais0, bais1] as InputStream[], ['hello', goodbye] as String[])

        then:
        simple.getInputStreams().length == 2
        simple.getInputStream() == bais0
        simple.getInputStreams()[0] == bais0
        simple.getInputStreams()[1] == bais1
        simple.getGunzippedInputStreams().length == 2
        simple.getGzippedInputStreams().length == 2
        isGzipped(simple.getGzippedInputStreams()[0])
        isGzipped(simple.getGzippedInputStreams()[1])
        !isGzipped(simple.getGunzippedInputStreams()[0])
        !isGzipped(simple.getGunzippedInputStreams()[1])
    }

    boolean isGzipped(input) {
        byte[] signature = new byte[2];
        input.read(signature)
        return signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b
    }

    class SimpleAbstractStreamType extends AbstractStreamType {

        SimpleAbstractStreamType(input) {
            super(input)
        }

        SimpleAbstractStreamType(inputs, names) {
            super(inputs, names)
        }
    }
}
