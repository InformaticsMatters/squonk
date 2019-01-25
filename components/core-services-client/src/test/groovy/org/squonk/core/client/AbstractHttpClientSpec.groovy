/*
 * Copyright (c) 2019 Informatics Matters Ltd.
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

package org.squonk.core.client

import groovy.json.JsonSlurper
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicNameValuePair
import spock.lang.Specification

class AbstractHttpClientSpec extends Specification {

    void "post simple"() {

        String text = 'Hello world!'
        def client = new AbstractHttpClient()

        when:
        InputStream is = client.executePostAsInputStream(
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

}
