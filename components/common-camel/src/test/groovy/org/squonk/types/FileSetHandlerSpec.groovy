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
class FileSetHandlerSpec extends Specification {

    static def msg1 = "hello world!"

    void "test write variable"() {

        def h = new FileSetHandler()
        def fileset1 = new FileSet()
        def file1 = fileset1.addFile("file1", "file1.txt", msg1.bytes)
        def ctx = new DummyContext()

        when:
        h.writeVariable(fileset1, ctx)
        def json = new String(ctx.bytes)

        then:
        JsonHandler.getInstance().objectFromJson(json, FileSet.class) != null
    }

    void "test read variable"() {

        def h = new FileSetHandler()
        def fileset1 = new FileSet()
        fileset1.addFile("file1", "file1.txt", msg1.bytes)
        def json = JsonHandler.instance.objectToBytes(fileset1)
        def ctx = new DummyContext(bytes: json)

        when:
        FileSet fileset2 = h.readVariable(ctx)

        then:
        fileset2 != null
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
