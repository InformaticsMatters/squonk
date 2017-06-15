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

package org.squonk.options

import org.squonk.types.NumberRange
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 15/01/16.
 */
class OptionDescriptorSpec extends Specification {

    void "test json"() {

        def std1 = new OptionDescriptor(Integer.class, "key", "label", "description", OptionDescriptor.Mode.User);

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        //println json
        def std2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:

        json != null
        std2 != null
        std2 instanceof OptionDescriptor
        std2.typeDescriptor.type == Integer.class

    }

    void "test values json"() {

        def std1 = new OptionDescriptor(Integer.class, "key", "label", "description", OptionDescriptor.Mode.User)
                .withDefaultValue(1)
                .withValues([1, 2, 3] as Integer[]);

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        //println json
        def std2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:

        json != null
        std2 != null
        std2 instanceof OptionDescriptor
        std2.typeDescriptor.type == Integer.class
        std2.values[0] == 1
        std2.values[1] == 2

    }


    void "test number range"() {

        def std1 = new OptionDescriptor(NumberRange.Integer.class, "key", "label", "description", OptionDescriptor.Mode.User)
            .withValues([new NumberRange.Integer(1, 10), new NumberRange.Integer(2, 10)] as NumberRange.Integer[] )
            .withDefaultValue(new NumberRange.Integer(1, 10))

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        //println json
        def std2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:

        json != null
        std2 != null
        std2 instanceof OptionDescriptor
        std2.typeDescriptor.type == NumberRange.Integer.class
        std2.defaultValue instanceof NumberRange.Integer
        std2.defaultValue.minValue == 1
        std2.defaultValue.maxValue == 10
    }



    void "test with subclass"() {

        def std1 = new OptionDescriptorSubclass(Integer.class, "key", "label", "description");
        std1.putProperty("planet", "earth")
        std1.putProperty("position", new Integer(3))

        when:
        def json = JsonHandler.getInstance().objectToJson(std1)
        //println json
        def std2 = JsonHandler.getInstance().objectFromJson(json, OptionDescriptor.class)

        then:

        json != null
        std2 != null
        std2 instanceof OptionDescriptorSubclass
        std2.typeDescriptor.type == Integer.class
        std2.getProperty("planet") == "earth"
        std2.getProperty("position") == 3
    }

}
