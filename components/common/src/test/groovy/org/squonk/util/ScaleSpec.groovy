package org.squonk.util

import org.squonk.io.DepictionParameters
import org.squonk.types.Scale
import org.squonk.types.io.JsonHandler
import spock.lang.Specification

import java.awt.Color

/**
 * Created by timbo on 03/10/16.
 */
class ScaleSpec extends Specification {

    void "to/from json"() {

        Scale scale1 = new Scale("foo", Color.RED, Color.GREEN, 0f, 100f, DepictionParameters.HighlightMode.direct, false)
        when:
        String json = JsonHandler.getInstance().objectToJson(scale1)
        println json
        Scale scale2 = JsonHandler.getInstance().objectFromJson(json, Scale.class)

        then:
        json != null
        scale2 != null
        scale2.isHighlightBonds() == false

    }


}
