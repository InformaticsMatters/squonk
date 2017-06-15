/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.squonk.core.client

import org.squonk.notebook.api.NotebookCanvasDTO
import org.squonk.notebook.api.NotebookDTO
import org.squonk.notebook.api.NotebookEditableDTO

import org.squonk.notebook.api.NotebookSavepointDTO
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 *
 * @author timbo
 */
@Stepwise
class NotebookRestClientSpec extends Specification {
    
    String username = 'squonkuser'

    @Shared NotebookRestClient client = ClientSpecBase.createNotebookRestClient()

    @Shared NotebookDTO notebook1
    @Shared NotebookEditableDTO editable1
    @Shared NotebookEditableDTO editable2
    @Shared NotebookSavepointDTO savepoint1


    void "list notebooks none exist"() {

        when:
        def notebooks = client.listNotebooks(username)
        
        then:
        notebooks != null
        notebooks.size() == 0
    }


    void "list editables notebook not exist"() {

        when:
        def editables = client.listEditables(0l, username)

        then:
        editables != null
        editables.size() == 0
    }

    void "list savepoints notebook not exist"() {

        when:
        def savepoints = client.listSavepoints(0l)

        then:
        savepoints != null
        savepoints.size() == 0
    }

    void "create notebook"() {

        when:
        notebook1 = client.createNotebook(username, "notebook1", "first notebook")

        then:
        notebook1 != null
        notebook1.id > 0
        notebook1.owner == username
    }

    void "create notebook bad user"() {

        when:
        def notebook = client.createNotebook("bananaman", "won't create", "user doesn't exist")

        then:
        thrown(IOException.class)
    }

    void "list notebooks"() {

        when:
        def notebooks = client.listNotebooks(username)

        then:
        notebooks != null
        notebooks.size() == 1
    }

    void "delete notebook"() {

        when:
        def notebook = client.createNotebook(username, "notebook99", "disposable notebook")
        int c1 = client.listNotebooks(username).size()
        client.deleteNotebook(notebook.id)
        int c2 = client.listNotebooks(username).size()

        then:
        c1-c2 == 1

    }

    void "update notebook"() {

        when:
        notebook1 = client.updateNotebook(notebook1.id, "different name", "different description")

        then:
        notebook1 != null
        notebook1.id > 0
        notebook1.name ==  "different name"
        notebook1.description ==  "different description"
    }

    void "add to layer"() {
        when:
        NotebookDTO nb = client.addNotebookToLayer(notebook1.id, "public")

        then:
        nb.layers.size() == 1
        nb.layers[0] == "public"
    }

    void "remove from layer"() {
        when:
        NotebookDTO nb = client.removeNotebookFromLayer(notebook1.id, "public")

        then:
        nb.layers.size() == 0
    }

    void "fetch initial editable"() {

        when:
        editable1 = client.listEditables(notebook1.id, username)[0]

        then:
        editable1 != null
        notebook1.id == editable1.notebookId
    }


    void "update editable"() {
        println "update editable()"

        when:
        editable1 = client.updateEditable(editable1.notebookId, editable1.id, new NotebookCanvasDTO(1))

        then:
        editable1 != null
        editable1.canvasDTO != null
    }

    void "create and delete editable"() {
        NotebookDTO notebook = client.createNotebook(username, "notebook99", "create and delete editable")
        Long nbid = notebook.id

        when:
        List<NotebookEditableDTO> eds0 = client.listEditables(nbid, username)
        NotebookEditableDTO ed0 = client.updateEditable(eds0[0].notebookId, eds0[0].id, new NotebookCanvasDTO(999))
        NotebookEditableDTO ed1 = client.createSavepoint(ed0.notebookId, ed0.id, "sp1") // savepoint now has the ID of the ed0
        List<NotebookEditableDTO> eds1 = client.listEditables(nbid, username)
        NotebookEditableDTO ed2 = client.createEditable(nbid, ed0.id, username)
        List<NotebookEditableDTO> eds2 = client.listEditables(nbid, username)
        client.deleteEditable(nbid, ed2.id, username)
        List<NotebookEditableDTO> eds3 = client.listEditables(nbid, username)


        then:
        ed0.canvasDTO.lastCellId == 999
        ed1.canvasDTO.lastCellId == 999
        ed2.canvasDTO.lastCellId == 999
        eds0.size() == 1
        eds1.size() == 1
        eds2.size() == 2
        eds3.size() == 1
    }

    void "create savepoint"() {

        when:
        editable2 = client.createSavepoint(editable1.notebookId, editable1.id, "a description")
        def sps = client.listSavepoints(editable1.notebookId)
        savepoint1 = sps[0]

        then:
        savepoint1 != null
        savepoint1.id == editable1.id
        editable2 != null
        editable2.id > 0
        editable2.owner == username
        editable2.parentId == editable1.id
    }

    void "set savepoint description"() {

        when:
        long id = savepoint1.id
        savepoint1 = client.setSavepointDescription(savepoint1.notebookId, savepoint1.id, "HelloWorld!")

        then:
        savepoint1 != null
        savepoint1.description == "HelloWorld!"
        id == savepoint1.id
    }

    void "set savepoint label"() {

        when:
        long id = savepoint1.id
        savepoint1 = client.setSavepointLabel(savepoint1.notebookId, savepoint1.id, "label1")

        then:
        savepoint1 != null
        savepoint1.label == "label1"
        id == savepoint1.id
    }

    void "write/read text variable"() {

        when:
        client.writeTextValue(editable2.notebookId, editable2.id, 1, "var1", "hello world")
        String s1 = client.readTextValue(editable2.notebookId, editable2.id, 1, "var1")
        String s2 = client.readTextValue(editable2.notebookId, editable2.id, 1, "var1", "default")

        then:
        s1 == "hello world"
        s2 == "hello world"
    }

    void "write/read stream variable"() {

        when:
        client.writeStreamValue(editable2.notebookId, editable2.id, 1, "var2", new ByteArrayInputStream("hello mars".bytes))
        String s1 = client.readStreamValue(editable2.notebookId, editable2.id, 1, "var2").text
        String s2 = client.readStreamValue(editable2.notebookId, editable2.id, 1, "var2", "default").text

        then:
        s1 == "hello mars"
        s2 == "hello mars"
    }

    void "delete variable"() {

        when:
        client.writeTextValue(editable2.notebookId, editable2.id, 1, "var3", "bananas")
        def t1 = client.readTextValue(editable2.notebookId, editable2.id, 1, "var3")
        client.deleteVariable(editable2.notebookId, editable2.id, 1, "var3")
        boolean deleted
        try {
            // this will now throw exception
            client.readTextValue(editable2.notebookId, editable2.id, 1, "var3")
            deleted = false
        } catch (Exception e) {
            deleted = true
        }

        then:
        t1 == "bananas"
        deleted == true
    }

//    void "read text variable with label"() {
//
//        def ed1 = client.createSavepoint(editable2.notebookId, editable2.id) // savepoint id is editable2.id
//        def ed2 = client.createSavepoint(ed1.notebookId, ed1.id) // savepoint id is ed1.id
//        def savepoint = client.setSavepointLabel(editable2.notebookId, ed1.id, "label2")
//
//        when:
//        String s1 = client.readTextValue(editable2.notebookId, 1, "label2", "var1")
//
//        then:
//        s1 == "hello world"
//    }
//
//    void "read stream variable with label"() {
//
//        when:
//        String s1 = client.readStreamValue(editable2.notebookId, 1, "label2", "var2").text
//
//        then:
//        s1 == "hello mars"
//    }
}

