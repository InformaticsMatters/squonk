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

package org.squonk.types.depict

import org.squonk.io.DepictionParameters
import org.squonk.types.Scale
import spock.lang.Specification

import java.awt.Color

/**
 * Created by timbo on 05/10/16.
 */
class HTMLRenderersSpec extends Specification {

    void "scale to html"() {
        when:
        Scale scale = new Scale("foo", Color.RED, Color.GREEN, 0f, 100f, DepictionParameters.HighlightMode.direct, false)
        def html = HTMLRenderers.instance.render(scale)
        println html

        then:
        html != null

    }
}
