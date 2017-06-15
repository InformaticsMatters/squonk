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

package org.squonk.dataset

import org.squonk.types.BasicObject
import spock.lang.Specification

/**
 * Created by timbo on 24/08/16.
 */
class DatasetUtilsSpec extends Specification {

    void "simple type merge"() {

        def meta1 = new DatasetMetadata(BasicObject.class, [a:String.class, b:Integer.class])
        def meta2 = new DatasetMetadata(BasicObject.class, [a:String.class, c:Integer.class])

        when:
        def meta3 = DatasetUtils.mergeDatasetMetadata(meta1, meta2)

        then:
        meta3.valueClassMappings.size() == 3
    }

    void "simple field merge"() {

        def meta1 = new DatasetMetadata(BasicObject.class, [a:String.class, b:Integer.class])

        meta1.putFieldMetaProp('a', DatasetMetadata.PROP_SOURCE, 'test1')
        meta1.appendFieldHistory('a', 'aaa')
        meta1.putFieldMetaProp('a', 'random', 2)


        def meta2 = new DatasetMetadata(BasicObject.class, [a:String.class, b:Integer.class])
        meta2.appendFieldHistory('a', 'bbb')
        meta2.putFieldMetaProp('a', 'random', 3)


        when:
        def meta3 = DatasetUtils.mergeDatasetMetadata(meta1, meta2)
        def aHistory = meta3.getFieldMetaProp('a', DatasetMetadata.PROP_HISTORY)
        def r = meta3.getFieldMetaProp('a', 'random')
        println aHistory
        def lines = aHistory.split "\n"

        then:
        meta3.valueClassMappings.size() == 2
        lines.size() == 2
        lines[0].endsWith('aaa')
        lines[1].endsWith('bbb')
        r == 5
    }

    void "simple dataset merge"() {

        def meta1 = new DatasetMetadata(BasicObject.class, [a:String.class, b:Integer.class])

        meta1.putFieldMetaProp('a', DatasetMetadata.PROP_SOURCE, 'test1')
        meta1.appendDatasetHistory('aaa')
        meta1.properties.put('random', 2)


        def meta2 = new DatasetMetadata(BasicObject.class, [a:String.class, b:Integer.class])
        meta2.appendDatasetHistory('bbb')
        meta2.properties.put('random', 3)


        when:
        def meta3 = DatasetUtils.mergeDatasetMetadata(meta1, meta2)
        def aHistory = meta3.properties.get(DatasetMetadata.PROP_HISTORY)
        def r = meta3.properties.get('random')
        println aHistory
        def lines = aHistory.split "\n"

        then:
        meta3.valueClassMappings.size() == 2
        lines.size() == 2
        lines[0].endsWith('aaa')
        lines[1].endsWith('bbb')
        r == 5
    }

}
