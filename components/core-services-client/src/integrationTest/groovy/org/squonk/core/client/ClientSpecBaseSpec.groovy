package org.squonk.core.client


class ClientSpecBaseSpec extends ClientSpecBase {

    void "test createNotebookRestClient"() {

        when:
        def client = createNotebookRestClient()

        then:
        client != null
    }

    void "test createJobStatusRestClient"() {

        when:
        def client = createJobStatusRestClient()

        then:
        client != null
    }

    void "test createServicesClient"() {

        when:
        def client = createServicesClient()

        then:
        client != null
    }

    void "test createUserRestClient"() {

        when:
        def client = createUserRestClient()

        then:
        client != null
    }


}
