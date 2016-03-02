package org.squonk.core.notebook.service

import org.squonk.core.util.TestUtils
import org.squonk.notebook.api2.NotebookDescriptor
import org.squonk.notebook.api2.NotebookEditable
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 29/02/2016.
 */
@Stepwise
class PostgresNotebookClientSpec extends Specification {

    static
    @Shared PostgresNotebookClient client = new PostgresNotebookClient()




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

        String term = '{"hello": "world"}'

        when:
        List<NotebookDescriptor> nbs = client.listNotebooks(TestUtils.TEST_USERNAME)
        List<NotebookEditable> eds = client.listEditables(nbs[0].id, TestUtils.TEST_USERNAME)
        NotebookEditable up = client.updateNotebookEditable(nbs[0].id, eds[0].id, term)

        then:
        eds.size() == 1
        up.owner == TestUtils.TEST_USERNAME
        up.parentId == null
        up.createdDate != null
        up.lastUpdatedDate != null
        up.content == term
        eds[0].createdDate == up.createdDate
        eds[0].lastUpdatedDate < up.lastUpdatedDate

    }
}
