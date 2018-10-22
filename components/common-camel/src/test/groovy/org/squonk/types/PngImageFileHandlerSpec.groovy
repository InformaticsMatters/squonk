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
import org.squonk.io.FileDataSource
import org.squonk.io.InputStreamDataSource
import org.squonk.io.SquonkDataSource
import spock.lang.Specification

/**
 * Created by timbo on 15/06/17.
 */
class PngImageFileHandlerSpec extends Specification {

    static String tmpdir = System.getProperty("java.io.tmpdir")

    void "test write variable"() {

        def h = new PngImageFileHandler()
        def data = new FileDataSource(null, null, new File("../../data/testfiles/image.png"), false)
        def png1 = new PngImageFile(data    )
        def ctx = new DummyContext()

        when:
        h.writeVariable(png1, ctx)

        then:
        ctx.bytes.length > 0
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
        SquonkDataSource readStreamValue(String mediaType, String extension, String key) throws Exception {
            def is = new ByteArrayInputStream(bytes)
            return new InputStreamDataSource(SquonkDataSource.ROLE_DEFAULT, null, mediaType, is, false)
        }
    }
}
