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
