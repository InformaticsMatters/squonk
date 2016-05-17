package org.squonk.core.service.notebook

import groovy.sql.Sql
import org.postgresql.util.PSQLException
import org.squonk.core.util.TestUtils
import org.squonk.notebook.api.NotebookCanvasDTO
import org.squonk.notebook.api.NotebookDTO
import org.squonk.notebook.api.NotebookEditableDTO
import org.squonk.notebook.api.NotebookSavepointDTO
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 29/02/2016.
 */
@Stepwise
class NotebookPostgresClientSpec extends Specification {

    @Shared NotebookPostgresClient client = new NotebookPostgresClient()
    @Shared List<NotebookDTO> notebooks

    static NotebookCanvasDTO CANVAS_DTO = new NotebookCanvasDTO(1)
    static String LONG_VARIABLE_1 = 'a potentially very long variable' * 10
    static String LONG_VARIABLE_2 = 'another potentially very long variable' * 10
    static String username = TestUtils.TEST_USERNAME



    void "create notebooks"() {

        setup:
        client.createSql().execute("DELETE FROM users.nb_descriptor")

        when:
        NotebookDTO nb1 = client.createNotebook(username, "notebook1", "notebook one")
        NotebookDTO nb2 = client.createNotebook(username, "notebook2", "notebook two")

        then:
        nb1 != null
        nb2 != null
        nb1.id > 0
        nb1.owner == username
        nb1.name == "notebook1"
        nb1.description == "notebook one"
        nb1.createdDate != null
        nb1.lastUpdatedDate != null
    }

    void "list notebooks"() {

        when:
        notebooks = client.listNotebooks(username)

        then:
        notebooks.size() == 2
        notebooks[0].name == "notebook2" // should be listed in descending creation date
        notebooks[1].name == "notebook1"
    }

    void "delete notebook"() {

        when:
        NotebookDTO nb3 = client.createNotebook(username, "notebook3", "notebook three")
        def notebooks1 = client.listNotebooks(username)
        client.deleteNotebook(nb3.id)
        def notebooks2 = client.listNotebooks(username)

        then:
        notebooks1.size() == 3
        notebooks2.size() == 2
    }


    void "add to layer"() {

        when:
        NotebookDTO nb = client.addNotebookToLayer(notebooks[0].id, "public")

        then:
        nb.layers.size() == 1
        nb.layers[0] == "public"
    }

    void "list public"() {

        when:
        notebooks = client.listNotebooks("another_user")

        then:
        notebooks.size() == 1
    }


    void "remove from layer"() {

        when:
        NotebookDTO nb = client.removeNotebookFromLayer(notebooks[0].id, "public")

        then:
        nb.layers.size() == 0
    }


    void "list editables"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)

        then:
        eds.size() == 1
        eds[0].owner == username
        eds[0].parentId == null
        eds[0].createdDate != null
        eds[0].lastUpdatedDate != null
    }

    void "update editable"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        NotebookEditableDTO up = client.updateEditable(notebooks[0].id, eds[0].id, new NotebookCanvasDTO(99))

        then:
        eds.size() == 1
        up.owner == username
        up.parentId == null
        up.createdDate != null
        up.lastUpdatedDate != null
        up.canvasDTO != null
        up.canvasDTO.lastCellId == 99
        eds[0].createdDate == up.createdDate
        eds[0].lastUpdatedDate < up.lastUpdatedDate
    }

    void "create savepoint"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        NotebookEditableDTO nue = client.createSavepoint(eds[0].notebookId, eds[0].id, "a description")
        List<NotebookSavepointDTO> sps = client.listSavepoints(notebooks[0].id)

        then:
        nue != null
        nue.owner == username
        nue.parentId == sps[0].id
        nue.createdDate != null
        nue.lastUpdatedDate != null
        nue.canvasDTO != null
        eds[0].createdDate < nue.createdDate
        eds[0].lastUpdatedDate < nue.lastUpdatedDate

        sps.size() == 1
        sps[0].id == eds[0].id
        sps[0].creator == username
        sps[0].canvasDTO != null
        sps[0].createdDate <= nue.createdDate
    }

    void "savepoint description"() {

        when:
        List<NotebookSavepointDTO> sps = client.listSavepoints(notebooks[0].id)
        NotebookSavepointDTO sp1 = client.setSavepointDescription(sps[0].notebookId, sps[0].id, "squonk")

        then:
        sp1.description == "squonk"
        sp1.creator == username
    }

    void "savepoint label"() {

        when:
        List<NotebookDTO> nbs = client.listNotebooks(username)
        List<NotebookSavepointDTO> sps = client.listSavepoints(nbs[0].id)
        NotebookSavepointDTO sp1 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, "label1")
        NotebookSavepointDTO sp2 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, null)
        NotebookSavepointDTO sp3 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, "label2")

        then:
        sps.size() == 1
        sp1.label == "label1"
        sp2.label == null
        sp3.label == "label2"
    }

    void "duplicate labels"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        NotebookEditableDTO nue = client.createSavepoint(eds[0].notebookId, eds[0].id, "a description")
        List<NotebookSavepointDTO> sps = client.listSavepoints(notebooks[0].id)

        sps.each {
            client.setSavepointLabel(it.notebookId, it.id, "label3")
        }

        then:
        thrown(PSQLException)
    }

    void "create and delete editable"() {
        NotebookDTO notebook = client.createNotebook(username, "notebook999", "create and delete editable")
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


    void "text variable insert no key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        client.writeTextValue(eds[0].notebookId, eds[0].id, 1, 'var1', 'val1')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 1
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='default' AND source_id=${eds[0].id}")[0] == 'val1'

    }

    void "text variable insert with key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        client.writeTextValue(eds[0].notebookId, eds[0].id, 1, 'var1', 'val2', 'key')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 2
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='key' AND source_id=${eds[0].id}")[0] == 'val2'

    }

    void "text variable update no key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        client.writeTextValue(eds[0].notebookId, eds[0].id, 1, 'var1', 'val3')
        client.writeTextValue(eds[0].notebookId, eds[0].id, 1, 'var2', 'another val')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 3
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='default' AND source_id=${eds[0].id}")[0] == 'val3'
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var2' AND var_key='default' AND source_id=${eds[0].id}")[0] == 'another val'

    }

    void "text variable update with key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        client.writeTextValue(eds[0].notebookId, eds[0].id, 1, 'var1', 'val4', 'key')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 3
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='key' AND source_id=${eds[0].id}")[0] == 'val4'

    }



    void "stream variable insert no key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].notebookId, eds[0].id, 1, 'stream1', new ByteArrayInputStream(LONG_VARIABLE_1.getBytes()), null)
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        (afterCount - beforeCount) == 1
    }

    void "stream variable insert with key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].notebookId, eds[0].id, 1, 'stream2', new ByteArrayInputStream(LONG_VARIABLE_1.getBytes()), "key")
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        (afterCount - beforeCount) == 1
    }

    void "stream variable update no key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].notebookId, eds[0].id, 1, 'stream1', new ByteArrayInputStream(LONG_VARIABLE_2.getBytes()), null)
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        afterCount == beforeCount
    }

    void "stream variable update with key"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].notebookId, eds[0].id, 1, 'stream2', new ByteArrayInputStream(LONG_VARIABLE_2.getBytes()), "key")
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        afterCount == beforeCount
    }

    void "read text variable with key correct version"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        String var1 = client.readTextValue(eds[0].notebookId, eds[0].id, 1, 'var1', 'key')

        then:
        var1 == 'val4'
    }

    void "read text variable no key correct version"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        String var1 = client.readTextValue(eds[0].notebookId, eds[0].id, 1, 'var1')
        String var2 = client.readTextValue(eds[0].notebookId, eds[0].id, 1, 'var2')

        then:
        var1 == 'val3'
        var2 == 'another val'
    }

    void "read stream variable with key correct version"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        InputStream var1 = client.readStreamValue(eds[0].notebookId, eds[0].id, 1, 'stream2', 'key')
        String s = var1.text
        var1.close()

        then:
        s == LONG_VARIABLE_2

    }

    void "read stream variable no key correct version"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        InputStream var1 = client.readStreamValue(eds[0].notebookId, eds[0].id, 1, 'stream1')
        String s = var1.text

        then:
        s == LONG_VARIABLE_2
    }

    void "read text variable previous version"() {

        when:
        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
        NotebookEditableDTO ed1 = client.createSavepoint(eds[0].notebookId, eds[0].id, "a description")

        // label it for later
        client.setSavepointLabel(eds[0].notebookId, eds[0].id, 'label4')

        String var1 = client.readTextValue(ed1.notebookId, ed1.id, 1, 'var1')
        NotebookEditableDTO ed2 = client.createSavepoint(ed1.notebookId, ed1.id, "a description")
        String var2 = client.readTextValue(ed2.notebookId, ed2.id, 1, 'var1')

        then:
        var1 == 'val3'
        var2 == 'val3'
    }

    void "read stream variable previous version"() {

        when:
        NotebookEditableDTO ed = client.listEditables(notebooks[0].id, username)[0]
        println "Editable ID=${ed.id}"

        InputStream var1 = client.readStreamValue(ed.notebookId, ed.id, 1, 'stream1')
        String s = var1.text
        var1.close()

        then:
        s == LONG_VARIABLE_2
    }

//    void "read text variable for label"() {
//
//        when:
//        List<NotebookEditableDTO> eds = client.listEditables(notebooks[0].id, username)
//        String var = client.readTextValue(notebooks[0].id, 'label4', 'var1', null)
//
//        then:
//        var == 'val3'
//    }

    void "delete stale variable"() {

        Sql db = client.createSql()

        when:
        NotebookEditableDTO ed = client.listEditables(notebooks[0].id, username)[0]
        client.writeTextValue(ed.notebookId, ed.id, 1, 'var99', 'val99')
        int c1 = db.firstRow("SELECT COUNT(*) FROM users.nb_variable WHERE source_id=${ed.id}")[0]
        // no cells so variables should be deleted
        client.updateEditable(ed.notebookId, ed.id, new NotebookCanvasDTO(1, 1))
        int c2 = db.firstRow("SELECT COUNT(*) FROM users.nb_variable WHERE source_id=${ed.id}")[0]

        then:
        c1 > 0  // there used to be variables
        c2 == 0 // but not they've been deleted as the cell no longer exists

        cleanup:
        db.close()
    }

    private void dumpNotebookDetails(Long nbid) {
        Sql db = client.createSql()
        db.eachRow("SELECT id, parent_id, type, nb_definition FROM users.nb_version WHERE notebook_id=$nbid") {
            println it
        }

    }

}
