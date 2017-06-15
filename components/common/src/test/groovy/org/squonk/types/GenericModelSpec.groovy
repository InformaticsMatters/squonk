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

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 18/10/2016.
 */
class GenericModelSpec extends Specification {

    void "to/from json"() {

        def value = new BigDecimal(100)
        def model1 = new GenericModel<BigDecimal>(value, ['val1', 'val2'] as String[])
        model1.setStream('val1', new ByteArrayInputStream())
        def json = JsonHandler.getInstance().objectToJson(model1)
        println json

        when:
        def model2 = JsonHandler.getInstance().objectFromJson(json, GenericModel.class)

        then:
        model2 != null
        value.equals(model2.getModelItem())
        model2.getStreamNames()[0] == 'val1'
        model2.getStreamNames()[1] == 'val2'
    }
}
