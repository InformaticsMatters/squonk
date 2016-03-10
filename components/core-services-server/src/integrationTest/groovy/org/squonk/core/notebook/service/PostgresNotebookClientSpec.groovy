package org.squonk.core.notebook.service

import org.postgresql.util.PSQLException
import org.squonk.core.util.TestUtils
import org.squonk.notebook.api2.NotebookDescriptor
import org.squonk.notebook.api2.NotebookEditable
import org.squonk.notebook.api2.NotebookSavepoint
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 29/02/2016.
 */
@Stepwise
class PostgresNotebookClientSpec extends Specification {

    @Shared PostgresNotebookClient client = new PostgresNotebookClient()
    @Shared List<NotebookDescriptor> notebooks

    static String JSON_TERM = '{"hello": "world"}'
    static String LONG_VARIABLE_1 = 'a potentially very long variable' * 10
    static String LONG_VARIABLE_2 = 'another potentially very long variable' * 10



    void "create notebooks"() {

        when:
        NotebookDescriptor nb1 = client.createNotebook(TestUtils.TEST_USERNAME, "notebook1", "notebook one")
        NotebookDescriptor nb2 = client.createNotebook(TestUtils.TEST_USERNAME, "notebook2", "notebook two")

        then:
        nb1 != null
        nb2 != null
        nb1.id > 0
        nb1.owner == TestUtils.TEST_USERNAME
        nb1.name == "notebook1"
        nb1.description == "notebook one"
        nb1.createdDate != null
        nb1.lastUpdatedDate != null
    }

    void "list notebooks"() {

        when:
        notebooks = client.listNotebooks(TestUtils.TEST_USERNAME)

        then:
        notebooks.size() == 2
        notebooks[0].name == "notebook2" // should be listed in descending creation date
        notebooks[1].name == "notebook1"

    }

    void "list editables"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)

        then:
        eds.size() == 1
        eds[0].owner == TestUtils.TEST_USERNAME
        eds[0].parentId == null
        eds[0].createdDate != null
        eds[0].lastUpdatedDate != null
    }

    void "update editable"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable up = client.updateNotebookEditable(notebooks[0].id, eds[0].id, JSON_TERM)

        then:
        eds.size() == 1
        up.owner == TestUtils.TEST_USERNAME
        up.parentId == null
        up.createdDate != null
        up.lastUpdatedDate != null
        up.content == JSON_TERM
        eds[0].createdDate == up.createdDate
        eds[0].lastUpdatedDate < up.lastUpdatedDate
    }

    void "create savepoint"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable nue = client.createSavepoint(eds[0].notebookId, eds[0].id)
        List<NotebookSavepoint> sps = client.listSavepoints(notebooks[0].id)

        then:
        nue != null
        nue.owner == TestUtils.TEST_USERNAME
        nue.parentId == sps[0].id
        nue.createdDate != null
        nue.lastUpdatedDate != null
        nue.content == JSON_TERM
        eds[0].createdDate < nue.createdDate
        eds[0].lastUpdatedDate < nue.lastUpdatedDate

        sps.size() == 1
        sps[0].id == eds[0].id
        sps[0].creator == TestUtils.TEST_USERNAME
        sps[0].content == eds[0].content
        sps[0].createdDate <= nue.createdDate
    }

    void "savepoint description"() {

        when:
        List<NotebookSavepoint> sps = client.listSavepoints(notebooks[0].id)
        NotebookSavepoint sp1 = client.setSavepointDescription(sps[0].notebookId, sps[0].id, "squonk")

        then:
        sp1.description == "squonk"
        sp1.creator == TestUtils.TEST_USERNAME
    }

    void "savepoint label"() {

        when:
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookSavepoint> sps = client.listSavepoints(nbs[0].id)
        NotebookSavepoint sp1 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, "label1")
        NotebookSavepoint sp2 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, null)
        NotebookSavepoint sp3 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, "label2")

        then:
        sps.size() == 1
        sp1.label == "label1"
        sp2.label == null
        sp3.label == "label2"
    }

    void "duplicate labels"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable nue = client.createSavepoint(eds[0].notebookId, eds[0].id)
        List<NotebookSavepoint> sps = client.listSavepoints(notebooks[0].id)

        sps.each {
            client.setSavepointLabel(it.notebookId, it.id, "label3")
        }

        then:
        thrown(PSQLException)
    }

    void "text variable insert no key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        client.writeTextValue(eds[0].id, 1, 'var1', 'val1')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 1
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='default' AND source_id=${eds[0].id}")[0] == 'val1'

    }

    void "text variable insert with key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        client.writeTextValue(eds[0].id, 1, 'var1', 'val2', 'key')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 2
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='key' AND source_id=${eds[0].id}")[0] == 'val2'

    }

    void "text variable update no key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        client.writeTextValue(eds[0].id, 1, 'var1', 'val3')
        client.writeTextValue(eds[0].id, 1, 'var2', 'another val')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 3
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='default' AND source_id=${eds[0].id}")[0] == 'val3'
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var2' AND var_key='default' AND source_id=${eds[0].id}")[0] == 'another val'

    }

    void "text variable update with key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        client.writeTextValue(eds[0].id, 1, 'var1', 'val4', 'key')

        then:
        client.createSql().firstRow("SELECT count(*) from users.nb_variable")[0] == 3
        client.createSql().firstRow("SELECT val_text from users.nb_variable WHERE var_name='var1' AND var_key='key' AND source_id=${eds[0].id}")[0] == 'val4'

    }



    void "stream variable insert no key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].id, 1, 'stream1', new ByteArrayInputStream(LONG_VARIABLE_1.getBytes()), null)
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        (afterCount - beforeCount) == 1
    }

    void "stream variable insert with key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].id, 1, 'stream2', new ByteArrayInputStream(LONG_VARIABLE_1.getBytes()), "key")
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        (afterCount - beforeCount) == 1
    }

    void "stream variable update no key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].id, 1, 'stream1', new ByteArrayInputStream(LONG_VARIABLE_2.getBytes()), null)
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        afterCount == beforeCount
    }

    void "stream variable update with key"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        int beforeCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]
        client.writeStreamValue(eds[0].id, 1, 'stream2', new ByteArrayInputStream(LONG_VARIABLE_2.getBytes()), "key")
        int afterCount = client.createSql().firstRow("SELECT count(*) FROM users.nb_variable")[0]

        then:
        afterCount == beforeCount
    }

    void "read text variable with key correct version"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        String var1 = client.readTextValue(eds[0].id, 'var1', 'key')

        then:
        var1 == 'val4'
    }

    void "read text variable no key correct version"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        String var1 = client.readTextValue(eds[0].id, 'var1')
        String var2 = client.readTextValue(eds[0].id, 'var2')

        then:
        var1 == 'val3'
        var2 == 'another val'
    }

    void "read stream variable with key correct version"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        InputStream var1 = client.readStreamValue(eds[0].id, 'stream2', 'key')
        String s = var1.text
        var1.close()

        then:
        s == LONG_VARIABLE_2

    }

    void "read stream variable no key correct version"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        InputStream var1 = client.readStreamValue(eds[0].id, 'stream1')
        String s = var1.text

        then:
        s == LONG_VARIABLE_2
    }

    void "read text variable previous version"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable ed1 = client.createSavepoint(eds[0].notebookId, eds[0].id)

        // label it for later
        client.setSavepointLabel(eds[0].notebookId, eds[0].id, 'label4')

        String var1 = client.readTextValue(ed1.id, 'var1')
        NotebookEditable ed2 = client.createSavepoint(ed1.notebookId, ed1.id)
        String var2 = client.readTextValue(ed2.id, 'var1')

        then:
        var1 == 'val3'
        var2 == 'val3'
    }

    void "read stream variable previous version"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)

        InputStream var1 = client.readStreamValue(eds[0].id, 'stream1')
        String s = var1.text
        var1.close()

        then:
        s == LONG_VARIABLE_2
    }

    void "read text variable for label"() {

        when:
        List<NotebookEditable> eds = client.listEditables(notebooks[0].id, TestUtils.TEST_USERNAME)
        String var = client.readTextValueForLabel(notebooks[0].id, 'label4', 'var1', null)

        then:
        var == 'val3'
    }

}
