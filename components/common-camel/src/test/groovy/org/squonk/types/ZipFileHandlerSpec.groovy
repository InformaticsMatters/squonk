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

import org.squonk.api.VariableHandler
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 15/06/17.
 */
class ZipFileHandlerSpec extends Specification {

    static String tmpdir = System.getProperty("java.io.tmpdir")

    void "test write variable"() {

        def h = new ZipFileHandler()
        def fis = new FileInputStream("../../data/testfiles/test.zip")
        def zip1 = new ZipFile(fis)
        def ctx = new DummyContext()

        when:
        h.writeVariable(zip1, ctx)

        then:
        ctx.bytes.length > 0

        cleanup:
        fis?.close()
    }

    void "test read variable"() {

        def h = new ZipFileHandler()
        def f = new java.io.File("../../data/testfiles/test.zip")
        def ctx = new DummyContext(bytes: f.bytes)

        when:
        ZipFile zip1 = h.readVariable(ctx)
        java.io.File zf = new java.io.File(tmpdir, "foo" + new Random().nextInt() + ".zip")
        zf.bytes = zip1.inputStream.bytes
        java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zf)

        then:
        zip1 != null
        zip.size() == 2

        cleanup:
        zf?.delete()
    }

    class DummyContext implements VariableHandler.WriteContext, VariableHandler.ReadContext{

        byte[] bytes
        String key


        @Override
        void writeTextValue(String value, String mediaType, String extension, String key) throws Exception {

        }

        @Override
        void writeStreamValue(InputStream value, String mediaType, String extension, String key, boolean gzip) throws Exception {
            this.bytes = value.bytes
            this.key = key
        }

        @Override
        void deleteVariable() throws Exception {

        }

        @Override
        String readTextValue(String mediaType, String extension, String key) throws Exception {
            return null
        }

        @Override
        InputStream readStreamValue(String mediaType, String extension, String key) throws Exception {
            return new ByteArrayInputStream(bytes)
        }
    }
}
