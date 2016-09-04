package org.squonk.util

import java.util.zip.GZIPOutputStream
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class IOUtilsSpec extends Specification {

    String hello = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"

    void "read bytes"() {

        byte[] input = "AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDDEEEEEEEEEE".bytes
        def is = new ByteArrayInputStream(input)
        byte[] buffer = new byte[2];
        int iters = 0

        when:
        while (true) {
            iters++
            int rsz = is.read(buffer);
            if (rsz < 0) {
                break;
            }
            String s = new String(buffer);
            //System.out.println("Writing " + rsz + " bytes" + s + " -> " + iters);
        }

        then:
        iters == 26

    }

    void "test simple compression"() {
        setup:
        //println "uncompressed length " + hello.getBytes().length

        when:
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        GZIPOutputStream gzipOS = new GZIPOutputStream(baos)
        gzipOS.write(hello.getBytes())
        gzipOS.close()
        byte[] gzipped = baos.toByteArray()


        then:
        gzipped.length > 0
        //println "gzipped length is " + gzipped.length

        gzipped[0] == (byte) 0x1f
        gzipped[1] == (byte) 0x8b

    }

    void "test getGzippedInputStream when source is gzipped"() {
        setup:
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        GZIPOutputStream gzipOS = new GZIPOutputStream(baos)
        gzipOS.write(hello.getBytes())
        gzipOS.close()
        byte[] gzipped = baos.toByteArray()


        when:
        InputStream converted = IOUtils.getGzippedInputStream(new ByteArrayInputStream(gzipped))
        byte[] signature = new byte[100];
        int size = converted.read(signature); //read the signature
        //println "size is " + size

        then:
        signature[0] == (byte) 0x1f
        signature[1] == (byte) 0x8b

        cleanup:
        converted.close()

    }

    void "test getGzippedInputStream when source is not gzipped"() {

        when:
        InputStream converted = IOUtils.getGzippedInputStream(new ByteArrayInputStream(hello.getBytes()))
        byte[] signature = new byte[100];
        int size = converted.read(signature); //read the signature
        //println "size is " + size

        then:
        signature[0] == (byte) 0x1f
        signature[1] == (byte) 0x8b

        cleanup:
        converted.close()

    }


    void "test truncate"() {

        expect:
        IOUtils.truncateString(str, 10) == result

        where:
        str             | result
        '0123456'       | '0123456'
        '01234567'      | '01234567'
        '012345678'     | '012345678'
        '0123456789'    | '0123456789'
        '01234567890'   | '012345 ...'
        '012345678901'  | '012345 ...'
        '0123456789012' | '012345 ...'
        null            | null

    }

}

