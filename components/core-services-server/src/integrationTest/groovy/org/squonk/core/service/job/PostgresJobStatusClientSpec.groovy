package org.squonk.core.service.job

import org.squonk.jobdef.DoNothingJobDefinition
import org.squonk.jobdef.JobDefinition
import org.squonk.jobdef.JobQuery
import org.squonk.jobdef.JobStatus
import org.squonk.core.util.TestUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * Created by timbo on 08/02/16.
 */
@Stepwise
class PostgresJobStatusClientSpec extends Specification {

    @Shared long inTheFuture = System.currentTimeMillis() + 1000000
    @Shared  PostgresJobStatusClient client = new PostgresJobStatusClient(TestUtils.createTestSquonkDataSource())

    void setupSpec() {
        client.db.executeUpdate("DELETE FROM users.jobstatus")
    }

    void "create job"() {

        JobDefinition jobdef = new DoNothingJobDefinition()

        when:
        int before = client.db.firstRow("SELECT count(*) from users.jobstatus")[0]
        JobStatus status = client.submit(jobdef, TestUtils.TEST_USERNAME, 100)
        int after = client.db.firstRow("SELECT count(*) from users.jobstatus")[0]


        then:
        status.status == JobStatus.Status.PENDING
        status.started != null
        after == before + 1
    }

    void "retrieve job"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status = client.get(uuid)

        then:
        status != null
        status.username == TestUtils.TEST_USERNAME
        status.jobDefinition instanceof DoNothingJobDefinition
    }

    void "update counts"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status1 = client.incrementCounts(uuid, 10, 5)
        JobStatus status2 = client.incrementCounts(uuid, 10, 5)

        then:
        status1.username == TestUtils.TEST_USERNAME
        status1.status == JobStatus.Status.PENDING
        status1.processedCount == 10
        status1.errorCount == 5

        status2.username == TestUtils.TEST_USERNAME
        status2.status == JobStatus.Status.PENDING
        status2.processedCount == 20
        status2.errorCount == 10
    }

    void "update status"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status = client.updateStatus(uuid, JobStatus.Status.RESULTS_READY)

        then:
        status != null
        status.username == TestUtils.TEST_USERNAME
        status.jobDefinition instanceof DoNothingJobDefinition
        status.status == JobStatus.Status.RESULTS_READY
        status.completed == null
    }

    void "first event"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status = client.updateStatus(uuid, null, "Going wrong")

        then:
        status != null
        status.username == TestUtils.TEST_USERNAME
        status.jobDefinition instanceof DoNothingJobDefinition
        status.status == JobStatus.Status.RESULTS_READY
        status.events.size() == 1
        status.events[0] == "Going wrong"
        status.completed == null
    }

    void "update event"() {

        String uuid = client.db.firstRow("SELECT uuid from users.jobstatus LIMIT 1")[0]

        when:
        JobStatus status = client.updateStatus(uuid, JobStatus.Status.ERROR, "Gone wrong")

        then:
        status != null
        status.username == TestUtils.TEST_USERNAME
        status.jobDefinition instanceof DoNothingJobDefinition
        status.status == JobStatus.Status.ERROR
        status.events.size() == 2
        status.events[0] == "Going wrong"
        status.events[1] == "Gone wrong"
        status.completed != null
    }

    void "list all statuses"() {

        when:
        List<JobStatus> statuses = client.list(null)

        then:
        statuses != null
        statuses.size() > 0
    }

    void "query statuses"() {

        expect:
        client.list(q).size() == c

        where:
        c|q
        1|new JobQuery(null, 100, [JobStatus.Status.ERROR, JobStatus.Status.COMPLETED], new java.sql.Date(0l), null, null, null)
        1|new JobQuery(null, null, [JobStatus.Status.ERROR, JobStatus.Status.COMPLETED], new java.sql.Date(0l), null, null, null)
        0|new JobQuery(null, 100, [JobStatus.Status.SUBMITTING], new java.sql.Date(0l), null, null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], new java.sql.Date(0l), null, null, null)
        0|new JobQuery('sombodyelse', 100, [JobStatus.Status.ERROR], new java.sql.Date(0l), null, null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], null, new java.sql.Date(inTheFuture), null, null)
        0|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], new java.sql.Date(inTheFuture), null, null, null)
        0|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], null, new java.sql.Date(0l), null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], new java.sql.Date(0l), new java.sql.Date(inTheFuture), null, null)
        1|new JobQuery(TestUtils.TEST_USERNAME, 100, [JobStatus.Status.ERROR], null, null, new java.sql.Date(0l), new java.sql.Date(inTheFuture))
    }

}
