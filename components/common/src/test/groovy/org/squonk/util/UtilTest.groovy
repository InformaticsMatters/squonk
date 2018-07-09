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

package org.squonk.util

import spock.lang.Specification

/**
 * Created by timbo on 05/05/17.
 */
class UtilTest extends Specification  {

    void "safeEqualsIncludeNull"() {

        expect:
        Utils.safeEqualsIncludeNull(a, b) == result

        where:
        a    | b    | result
        '0'  | '0'  | true
        '0'  | '1'  | false
        '0'  | null | false
        null | '1'  | false
        null | null | true

    }

    static InputStream bais = new ByteArrayInputStream('hello'.bytes)

    void "instantiate"() {

        expect:
        Utils.instantiate(type, [argType] as Class[], [value] as Object[]) != null

        where:
        type | argType | value
        InputStreamReader.class | InputStream.class | bais

    }
}
