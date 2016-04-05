package org.squonk.notebook.api

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 11/03/16.
 */
class NotebookSavepointDTOSpec extends Specification {

    void "to and from json"() {

        when:
        NotebookSavepointDTO s1 = new NotebookSavepointDTO(1, 2, 3, "creator", new Date(), new Date(), "description", "label", new NotebookCanvasDTO(1))
        String json = JsonHandler.getInstance().objectToJson(s1)
        NotebookSavepointDTO s2 = JsonHandler.getInstance().objectFromJson(json, NotebookSavepointDTO.class)

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
        s2.canvasDTO != null
    }

}
