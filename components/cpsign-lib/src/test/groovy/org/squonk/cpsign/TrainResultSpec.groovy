package org.squonk.cpsign

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 21/10/2016.
 */
class TrainResultSpec extends Specification {

    void "to/from json"() {

        TrainResult tr1 = new TrainResult(
                TrainResult.Method.CCP,
                TrainResult.Type.Classification,
                TrainResult.Library.LibLinear,
                1,3,5,0.5, 0.6, 0.7, "foo")

        String json = JsonHandler.getInstance().objectToJson(tr1)

        when:
        TrainResult tr2 =  JsonHandler.getInstance().objectFromJson(json, TrainResult.class)

        then:
        tr2 != null
        tr2.library == TrainResult.Library.LibLinear


    }
}
