package org.squonk.notebook.api2

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 11/03/16.
 */
class NotebookDescriptorSpec extends Specification {

    void "to and from json"() {

        when:
        NotebookDescriptor d1 = new NotebookDescriptor(1, "name", "description", "owner", new Date(), new Date())
        String json = JsonHandler.getInstance().objectToJson(d1)
        NotebookDescriptor d2 = JsonHandler.getInstance().objectFromJson(json, NotebookDescriptor.class)

        then:
        json != null
        json.length() > 0
        d2 != null
        d2.id == 1
        d2.name == "name"
        d2.description == "description"
        d2.owner == "owner"
        d2.createdDate != null
        d2.lastUpdatedDate != null
    }

}
