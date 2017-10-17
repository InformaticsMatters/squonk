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
import spock.lang.Specification

/**
 * Created by timbo on 15/06/17.
 */
class PngImageFileHandlerSpec extends Specification {

    static String tmpdir = System.getProperty("java.io.tmpdir")

    void "test write variable"() {

        def h = new PngImageFileHandler()
        def fis = new FileInputStream("../../data/testfiles/image.png")
        def png1 = new PngImageFile(fis)
        def ctx = new DummyContext()

        when:
        h.writeVariable(png1, ctx)

        then:
        ctx.bytes.length > 0

        cleanup:
        fis?.close()
    }

    void "test read variable"() {

        def h = new PngImageFileHandler()
        def f = new java.io.File("../../data/testfiles/image.png")
        def ctx = new DummyContext(bytes: f.bytes)

        when:
        PngImageFile png1 = h.readVariable(ctx)

        then:
        png1 != null
        png1.inputStream.bytes.length == 37607
    }

    class DummyContext implements VariableHandler.WriteContext, VariableHandler.ReadContext{

        byte[] bytes
        String key


        @Override
        void writeTextValue(String value, String key) throws Exception {

        }

        @Override
        void writeStreamValue(InputStream value, String key) throws Exception {
            this.bytes = value.bytes
            this.key = key
        }

        @Override
        void deleteVariable() throws Exception {

        }

        @Override
        String readTextValue(String key) throws Exception {
            return null
        }

        @Override
        InputStream readStreamValue(String key) throws Exception {
            return new ByteArrayInputStream(bytes)
        }
    }
}
