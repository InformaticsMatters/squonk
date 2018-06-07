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

package org.squonk.rdkit.services

import org.squonk.core.HttpServiceDescriptor
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

import java.util.stream.Stream

/**
 * Created by timbo on 12/02/16.
 */
class RdkitRestRouteBuilderSpec extends Specification  {

    void "calculators service descriptors to/from json"() {

        when:
        String json = JsonHandler.getInstance().objectToJson(RdkitBasicRestRouteBuilder.CALCULATORS_SERVICE_DESCRIPTORS)
        Stream<HttpServiceDescriptor> sds = JsonHandler.getInstance().streamFromJson(json, HttpServiceDescriptor.class)

        then:
        json.length() > 0
        sds.count() > 0

    }
}
