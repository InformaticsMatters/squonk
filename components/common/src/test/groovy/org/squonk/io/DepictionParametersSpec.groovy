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

package org.squonk.io

import spock.lang.Specification

import java.awt.Color

/**
 * Created by timbo on 29/09/2016.
 */
class DepictionParametersSpec extends Specification {

    void "test atom highlight"() {

        DepictionParameters dp = new DepictionParameters(100, 100, true, Color.RED)
            .addAtomHighlight([1,3] as int[], Color.GREEN, DepictionParameters.HighlightMode.region, false)

        when:
        QueryParams qps = dp.asQueryParams()

        then:
        qps.pairs.size() == 5
        qps.pairs[-1].toString() == 'atom_highlight_region_#ff00ff00=1,3'
    }

    void "from http params"() {

        Map<String,String[]> params = [
                w:['100'] as String[],
                h:['200'] as String[],
                expand:['true'] as String[],
                bg:['#ffffffff'] as String[],
                'atom_highlight_region_#ff00ff00': ['1,3'] as String[],
                'atom_highlight_direct_#ff00ff00': ['2,4'] as String[]
        ]

        when:
        DepictionParameters dp = DepictionParameters.fromHttpParams(params)

        then:
        dp != null
        dp.getWidth() == 100
        dp.getHeight() == 200
        dp.isExpandToFit()
        dp.getBackgroundColor().getRed() == 255
        dp.getHighlights().size() == 2
        dp.getHighlights()[0].toString() == 'atom_highlight_region_#ff00ff00=1,3'
        dp.getHighlights()[1].toString() == 'atom_highlight_direct_#ff00ff00=2,4'


    }


}
