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
