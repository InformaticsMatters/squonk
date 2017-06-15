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

package org.squonk.http

import spock.lang.Specification

/**
 * Created by timbo on 25/03/2016.
 */
class RequestInfoSpec extends Specification {

    void "findType"() {

        expect:
        RequestInfo.findType(r, w) == t

        where:
        r << [
                'application/json',
                'application/json',
                'application/json; q=1',
                'application/jsonnnnn',
                'text/plain,application/json',
                'text/plain, application/json'
        ]
        w << [
                ['application/json'] as String[],
                ['text/plain','application/json'] as String[],
                ['text/plain','application/json'] as String[],
                ['application/json'] as String[],
                ['application/json'] as String[],
                ['application/json'] as String[]
        ]
        t << [
                'application/json',
                'application/json',
                'application/json',
                null,
                'application/json',
                'application/json'
        ]
    }

}
