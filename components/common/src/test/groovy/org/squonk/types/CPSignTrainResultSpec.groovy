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

import org.squonk.types.CPSignTrainResult
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 21/10/2016.
 */
class CPSignTrainResultSpec extends Specification {

    void "to/from json"() {

        CPSignTrainResult tr1 = new CPSignTrainResult(
                CPSignTrainResult.Method.CCP,
                CPSignTrainResult.Type.Classification,
                CPSignTrainResult.Library.LibLinear,
                1,3,5,0.5, 0.6, 0.7, "foo")

        String json = JsonHandler.getInstance().objectToJson(tr1)

        when:
        CPSignTrainResult tr2 =  JsonHandler.getInstance().objectFromJson(json, CPSignTrainResult.class)

        then:
        tr2 != null
        tr2.library == CPSignTrainResult.Library.LibLinear


    }
}
