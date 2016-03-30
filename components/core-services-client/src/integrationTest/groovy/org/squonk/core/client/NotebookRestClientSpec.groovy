package org.squonk.core.client

import org.squonk.notebook.api2.NotebookDescriptor
import org.squonk.notebook.api2.NotebookEditable
import org.squonk.notebook.api2.NotebookInstance
import org.squonk.notebook.api2.NotebookSavepoint
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

    @Shared NotebookRestClient client = new NotebookRestClient()

    @Shared NotebookDescriptor notebook1
    @Shared NotebookEditable editable1
    @Shared NotebookEditable editable2
    @Shared NotebookEditable editable3
    @Shared NotebookSavepoint savepoint1


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


    void "update notebook"() {

        when:
        notebook1 = client.updateNotebook(notebook1.id, "different name", "different description")

        then:
        notebook1 != null
        notebook1.id > 0
        notebook1.name ==  "different name"
        notebook1.description ==  "different description"
    }

    void "fetch initial editable"() {

        when:
        editable1 = client.listEditables(notebook1.id, username)[0]

        then:
        editable1 != null
        notebook1.id == editable1.notebookId
    }

    void "create editable"() {

        when:
        editable2 = client.createEditable(notebook1.id, null, username)

        then:
        editable2 != null
        editable2.id > 0
        editable2.owner == username
    }

    void "update editable"() {

        when:
        editable2 = client.updateEditable(editable2.notebookId, editable2.id, new NotebookInstance())

        then:
        editable2 != null
        editable2.content != null
    }

    void "create savepoint"() {

        when:
        editable3 = client.createSavepoint(editable2.notebookId, editable2.id)
        def sps = client.listSavepoints(editable2.notebookId)
        savepoint1 = sps[0]

        then:
        savepoint1 != null
        savepoint1.id == editable2.id
        editable3 != null
        editable3.id > 0
        editable3.owner == username
        editable3.parentId == editable2.id
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
        client.writeTextValue(editable3.notebookId, editable3.id, 1, "var1", "hello world")
        String s1 = client.readTextValue(editable3.notebookId, editable3.id, "var1")
        String s2 = client.readTextValue(editable3.notebookId, editable3.id, "var1", "default")

        then:
        s1 == "hello world"
        s2 == "hello world"
    }

    void "write/read stream variable"() {

        when:
        client.writeStreamValue(editable3.notebookId, editable3.id, 1, "var2", new ByteArrayInputStream("hello mars".bytes))
        String s1 = client.readStreamValue(editable3.notebookId, editable3.id, "var2").text
        String s2 = client.readStreamValue(editable3.notebookId, editable3.id, "var2", "default").text

        then:
        s1 == "hello mars"
        s2 == "hello mars"
    }

    void "read text variable with label"() {

        def ed1 = client.createSavepoint(editable3.notebookId, editable3.id) // savepoint id is editable3.id
        def ed2 = client.createSavepoint(ed1.notebookId, ed1.id) // savepoint id is ed1.id
        def savepoint = client.setSavepointLabel(editable3.notebookId, ed1.id, "label2")

        when:
        String s1 = client.readTextValue(editable3.notebookId, "label2", "var1")

        then:
        s1 == "hello world"
    }

    void "read stream variable with label"() {

        when:
        String s1 = client.readStreamValue(editable3.notebookId, "label2", "var2").text

        then:
        s1 == "hello mars"
    }
}

