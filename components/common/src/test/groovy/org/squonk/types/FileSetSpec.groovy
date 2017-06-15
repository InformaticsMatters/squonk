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

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

import java.util.zip.GZIPInputStream

/**
 * Created by timbo on 15/06/17.
 */
class FileSetSpec extends Specification {

    static def msg1 = "hello world!"
    static def msg2 = "hello mars!"

    private String readAndGunzip(byte[] bytes) {
        return new String(new GZIPInputStream(new ByteArrayInputStream(bytes)).bytes)
    }


    void "test write to json"() {

        def fileset1 = new FileSet()
        def file1 = fileset1.addFile("file1", "file1.txt", msg1.bytes)
        def file2 = fileset1.addFile("file2", "file2.bin", msg2.bytes)


        ObjectMapper mapper = new ObjectMapper()

        when:
        def json = mapper.writeValueAsString(fileset1)
        println json
        def fileset2 = mapper.readValue(json, FileSet.class)

        then:
        fileset2.files.size() == 2
        fileset2.files[0].id == file1.id
        fileset2.files[0].filename == file1.filename
        readAndGunzip(fileset2.files[0].content) == msg1
        fileset2.files[1].id == file2.id
        fileset2.files[1].filename == file2.filename
        readAndGunzip(fileset2.files[1].content) == msg2
    }

    void "test write to files compressed"() {
        def fileset1 = new FileSet()
        def file1 = fileset1.addFile("file1", "file1.txt", msg1.bytes)
        def file2 = fileset1.addFile("file2", "file2.bin", msg2.bytes)
        def tmpdir = System.getProperty("java.io.tmpdir")
        println "Writing to $tmpdir"

        when:
        java.io.File dir = new java.io.File(tmpdir)
        file1.write(dir, false)
        file2.write(dir, false)

        then:
        java.io.File f1 = new java.io.File(dir, file1.filename + ".gz")
        f1.exists()
        readAndGunzip(f1.bytes) == msg1
        java.io.File f2 = new java.io.File(dir, file2.filename + ".gz")
        f2.exists()
        readAndGunzip(f2.bytes) == msg2

        cleanup:
        if (f1) f1.delete()
        if (f2) f2.delete()
        if (dir) dir.delete()
    }

    void "test write to files uncompressed"() {
        def fileset1 = new FileSet()
        def file1 = fileset1.addFile("file1", "file1.txt", msg1.bytes)
        def file2 = fileset1.addFile("file2", "file2.bin", msg2.bytes)
        def tmpdir = System.getProperty("java.io.tmpdir")
        println "Writing to $tmpdir"

        when:
        java.io.File dir = new java.io.File(tmpdir)
        file1.write(dir, true)
        file2.write(dir, true)

        then:
        java.io.File f1 = new java.io.File(dir, file1.filename)
        f1.exists()
        new String(f1.bytes) == msg1
        java.io.File f2 = new java.io.File(dir, file2.filename)
        f2.exists()
        new String(f2.bytes) == msg2

        cleanup:
        if (f1) f1.delete()
        if (f2) f2.delete()
        if (dir) dir.delete()
    }

}
