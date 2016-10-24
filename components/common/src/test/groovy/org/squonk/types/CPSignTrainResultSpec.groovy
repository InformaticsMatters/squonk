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
