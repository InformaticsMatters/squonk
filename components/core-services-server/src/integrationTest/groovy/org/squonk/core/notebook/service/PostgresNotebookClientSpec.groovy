package org.squonk.core.notebook.service

import com.ibm.db2.jcc.am.SqlException
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

    static String JSON_TERM = '{"hello": "world"}'



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
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)

        then:
        nbs.size() == 2
        nbs[0].name == "notebook2" // should be listed in descending creation date
        nbs[1].name == "notebook1"

    }

    void "list editables"() {

        when:
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookEditable> eds = client.listEditables(nbs[0].id, TestUtils.TEST_USERNAME)

        then:
        eds.size() == 1
        eds[0].owner == TestUtils.TEST_USERNAME
        eds[0].parentId == null
        eds[0].createdDate != null
        eds[0].lastUpdatedDate != null
    }

    void "update editable"() {

        when:
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookEditable> eds = client.listEditables(nbs[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable up = client.updateNotebookEditable(nbs[0].id, eds[0].id, JSON_TERM)

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
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookEditable> eds = client.listEditables(nbs[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable nue = client.createSavepoint(eds[0].notebookId, eds[0].id)
        List<NotebookSavepoint> sps = client.listSavepoints(nbs[0].id)

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
        sps[0].owner == TestUtils.TEST_USERNAME
        sps[0].content == eds[0].content
    }

    void "savepoint description"() {

        when:
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookSavepoint> sps = client.listSavepoints(nbs[0].id)
        NotebookSavepoint sp1 = client.setSavepointDescription(sps[0].notebookId, sps[0].id, "squonk")

        then:
        sp1.description == "squonk"
    }

    void "savepoint label"() {

        when:
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookSavepoint> sps = client.listSavepoints(nbs[0].id)
        NotebookSavepoint sp1 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, "squonk")
        NotebookSavepoint sp2 = client.setSavepointLabel(sps[0].notebookId, sps[0].id, null)

        then:
        sps.size() == 1
        sp1.label == "squonk"
        sp2.label == null
    }

    void "duplicate labels"() {

        when:
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookEditable> eds = client.listEditables(nbs[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable nue = client.createSavepoint(eds[0].notebookId, eds[0].id)
        List<NotebookSavepoint> sps = client.listSavepoints(nbs[0].id)

        sps.each {
            client.setSavepointLabel(it.notebookId, it.id, "abcdefg")
        }


        then:
        thrown(PSQLException)
    }

}
