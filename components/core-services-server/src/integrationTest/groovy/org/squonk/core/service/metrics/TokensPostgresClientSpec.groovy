package org.squonk.core.service.metrics

import groovy.sql.Sql
import org.squonk.core.util.TestUtils
import org.squonk.util.ExecutionStats
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 28/06/16.
 */
@Stepwise
class TokensPostgresClientSpec extends Specification{

    static String username = TestUtils.TEST_USERNAME

    @Shared TokensPostgresClient client = new TokensPostgresClient()
    @Shared String jobId = UUID.randomUUID().toString()

    void setupSpec() {

        // need to create a dummy job status
        Sql db = client.createSql()
        try {
            db.executeInsert("""INSERT INTO users.jobstatus (owner_id, uuid, status, total_count, processed_count, error_count, started, definition)
                    VALUES (1, $jobId, 'COMPLETED', 0, 0, 0, NOW(), '[]'::jsonb)""")
        } finally {
            db.close();
        }

    }

    void "retrieve token cost history"() {

        when:
        def results = client.getCostHistory("foo")

        then:
        results.size() == 0
    }

    void "create cost"() {

        when:
        client.updateCost("foo", 1.1f)
        def results = client.getCostHistory("foo")
        def current = client.getCurrentCost("foo")

        then:
        results.size() == 1
        results[0].key == "foo"
        current.cost == 1.1f
        current.created != null
        current.key == "foo"

    }

    void "update cost"() {

        when:
        client.updateCost("foo", 2.2f)
        def results = client.getCostHistory("foo")
        def current = client.getCurrentCost("foo")

        then:
        results.size() == 2
        results[0].cost == 2.2f
        results[1].cost == 1.1f
        current.cost == 2.2f
        current.created != null
        current.key == "foo"
    }

    void "initial user token usage"() {

        when:
        def usage = client.getUserTokenUsage(username)

        then:
        usage.size() == 0
    }

    void "add token usage single"() {

        when:
        client.saveExecutionStats(new ExecutionStats(jobId, [foo:9]))
        client.saveExecutionStats(new ExecutionStats(jobId, [foo:10]))
        client.saveExecutionStats(new ExecutionStats(jobId, [foo:11]))
        client.saveExecutionStats(new ExecutionStats(jobId, [bar:10]))
        def usage = client.getUserTokenUsage(username)

        then:
        // usage comes back in descending order (most recent first)
        usage.size() == 4
        usage[0].units == 10
        usage[0].tokenCount == null // no unit cost defined so token usage should be null
        usage[1].units == 11
        usage[1].tokenCount == 24.2f
        usage[2].units == 10
        usage[2].tokenCount == 22.0f
        usage[3].units == 9
        usage[3].tokenCount == 19.8f

    }

    void "add token usage batch"() {

        when:

        client.saveExecutionStats(new ExecutionStats(jobId, [foo:1, bar:2, baz:3]))
        def usage = client.getUserTokenUsage(username)
        println usage

        then:
        // usage comes back in descending order (most recent first)
        usage.size() == 7 // the previous 4 plus these 3

    }

}
