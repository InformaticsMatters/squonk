package org.squonk.notebook.api

import org.squonk.types.io.JsonHandler
import spock.lang.Specification

/**
 * Created by timbo on 11/03/16.
 */
class NotebookEditableDTOSpec extends Specification {

    void "to and from json"() {

        when:
        NotebookEditableDTO e1 = new NotebookEditableDTO(1, 2, 3, "owner", new Date(), new Date(), new NotebookCanvasDTO(1))
        String json = JsonHandler.getInstance().objectToJson(e1)
        println json
        NotebookEditableDTO e2 = JsonHandler.getInstance().objectFromJson(json, NotebookEditableDTO.class)

        then:
        json != null
        json.length() > 0
        e2 != null
        e2.id == 1
        e2.notebookId == 2
        e2.parentId == 3
        e2.owner == "owner"
        e2.createdDate != null
        e2.lastUpdatedDate != null
        e2.canvasDTO != null
    }

}
