package org.squonk.dataset

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 03/10/16.
 */
class DatasetSelectionSpec extends Specification {

    void "to/from json"() {

        List uuids = []
        uuids.add(UUID.randomUUID())
        uuids.add(UUID.randomUUID())
        DatasetSelection sel1 = new DatasetSelection(uuids)

        when:
        String json = JsonHandler.getInstance().objectToJson(sel1)
        DatasetSelection sel2 = JsonHandler.getInstance().objectFromJson(json, DatasetSelection.class)

        then:
        json != null
        sel2 != null
        sel2.uuids.size() == 2


    }
}
