package org.squonk.notebook.api2

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 11/03/16.
 */
class NotebookSavepointSpec extends Specification {

    void "to and from json"() {

        when:
        NotebookSavepoint s1 = new NotebookSavepoint(1, 2, 3, "creator", new Date(), new Date(), "description", "label", new NotebookInstance())
        String json = JsonHandler.getInstance().objectToJson(s1)
        NotebookSavepoint s2 = JsonHandler.getInstance().objectFromJson(json, NotebookSavepoint.class)

        then:
        json != null
        json.length() > 0
        s2 != null
        s2.id == 1
        s2.notebookId == 2
        s2.parentId == 3
        s2.creator == "creator"
        s2.createdDate != null
        s2.lastUpdatedDate != null
        s2.description == "description"
        s2.label == "label"
        s2.content != null
    }

}
