package org.squonk.core.job.service;

import com.im.lac.job.jobdef.JobDefinition
import com.im.lac.job.jobdef.JobQuery
import com.im.lac.job.jobdef.JobStatus
import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.client.JobStatusClient
import org.squonk.core.util.Utils
import org.squonk.types.io.JsonHandler

import java.sql.SQLException
import java.sql.Timestamp;


/**
 * Created by timbo on 04/01/16.
 */
@Log
public class PostgresJobStatusClient implements JobStatusClient {

    public final static PostgresJobStatusClient INSTANCE = new PostgresJobStatusClient();

    private final Object lock = new Object();

    protected final Sql db = new Sql(Utils.createDataSource())

    def execute(Closure cl) {
        def result
        db.withTransaction {
            result = cl(db)
        }
        return result
    }


    public JobStatus submit(JobDefinition jobdef, String username, Integer totalCount) {
        log.info("Registering JobDef: " + jobdef);
        JobStatus status = JobStatus.create(jobdef, username, new Date(), totalCount == null ? 0 : totalCount)
        int count
        db.withTransaction {
            count = insertStatusInDb(db, status)
        }
        if (count == 1) {
            return status
        } else {
            throw new SQLException("Failed to insert JobStatus into database - user $username might not exist?")
        }
    }

    private static final String SQL_INSERT = """INSERT INTO users.jobstatus (owner_id, uuid, status, total_count, processed_count, error_count, definition)
(SELECT id, ?, ?, ?, ?, ?, ?::jsonb FROM users.users u WHERE u.username = ?)"""

    private int insertStatusInDb(Sql db, JobStatus status) {
        String json = JsonHandler.getInstance().objectToJson(status.getJobDefinition())
            def vals = db.executeInsert(SQL_INSERT, [status.getJobId(), status.getStatus().toString(), status.totalCount, 0, 0, json, status.username])
            return vals.size()
    }

    private int updateStatusInDb(Sql db, String jobid, JobStatus.Status status, int processedCount, int errorCount, boolean complete) {
            def vals = db.executeInsert(
                    "UPDATE users.jobstatus j SET status=?, processed_count=d.p, error_count=d.e, completed=" +
                            (complete ? "NOW()" : "NULL") +
                            "\n  FROM (SELECT processed_count + ? AS p, error_count + ? AS e FROM users.jobstatus WHERE uuid =?) AS d" +
                            "\n  WHERE j.uuid=?",
                    [status.toString(), processedCount, errorCount, jobid, jobid])

            return vals.size()
    }


    @Override
    public JobStatus get(String id) {
        log.info("getting JobStatus for job $id");
        JobStatus result = null
        db.withTransaction {
            result = getFromDb(db, id)
        }
        return result
    }

    private static final String SQL_FETCH = "SELECT j.*, u.username FROM users.jobstatus j JOIN users.users u ON u.id = j.owner_id WHERE uuid = ?"

    private JobStatus getFromDb(Sql sql, String id) {
        def data = db.firstRow(SQL_FETCH, [id])
        if (data == null) {
            return null;
        } else {
            return buildJobStatusFromRow(data)
        }
    }

    private JobStatus buildJobStatusFromRow(def data) {
        String jobId = data['uuid']
        String username = data['username']
        JobStatus.Status status = JobStatus.Status.valueOf(data['status'])
        Integer totalCount = data['total_count']
        Integer processedCount = data['processed_count']
        Integer errorCount = data['error_count']
        Date started = data['started']
        Date completed = data['completed']
        String json = data['definition']
        String[] events = data['events']
        JobDefinition jobDefinition = JsonHandler.getInstance().objectFromJson(json, JobDefinition.class)

        return new JobStatus(jobId, username, status,
                totalCount == null ? 0 : totalCount.intValue(),
                processedCount == null ? 0 : processedCount.intValue(),
                errorCount == null ? 0 : errorCount.intValue(),
                started,
                completed,
                jobDefinition, null,
                events == null ? null : Arrays.asList(events))
    }

    private static final String SQL_QUERY = '''SELECT j.*, u.username FROM users.jobstatus j JOIN users.users u ON u.id = j.owner_id WHERE
(:owner::text IS NULL OR u.username = :owner) AND
(:statuses::text[] IS NULL OR status = ANY (:statuses)) AND
(:submissionTimeStart::timestamp IS NULL OR started   >= :submissionTimeStart) AND (:submissionTimeEnd::timestamp IS NULL OR started   <= :submissionTimeEnd) AND
(:completionTimeStart::timestamp IS NULL OR completed >= :completionTimeStart) AND (:completionTimeEnd::timestamp IS NULL OR completed <= :completionTimeEnd)
'''

    @Override
    public List<JobStatus> list(JobQuery query) {
        if (query == null) {
            query = new JobQuery()
        }

        Timestamp submissionTimeStart = (query.submissionTimeStart == null ? null : new Timestamp(query.submissionTimeStart.getTime()))
        Timestamp submissionTimeEnd = (query.submissionTimeEnd == null ? null : new Timestamp(query.submissionTimeEnd.getTime()))
        Timestamp completionTimeStart = (query.completionTimeStart == null ? null : new Timestamp(query.completionTimeStart.getTime()))
        Timestamp completionTimeEnd = (query.completionTimeEnd == null ? null : new Timestamp(query.completionTimeEnd.getTime()))
        String[] statuses = (query.statuses == null ? null : query.statuses.collect() { it.toString() })

        List<JobStatus> results = new ArrayList<>();
        db.withTransaction {
            def arr = (statuses == null ? null : db.connection.createArrayOf("text", statuses))
            db.eachRow(SQL_QUERY + (query.max == null ? "" : " LIMIT ${query.max}"),
                    [owner:query.username,
                     statuses:arr,
                     submissionTimeStart:submissionTimeStart, submissionTimeEnd:submissionTimeEnd,
                     completionTimeStart:completionTimeStart, completionTimeEnd:completionTimeEnd
                    ]) {

            results << buildJobStatusFromRow(it)

            }
        }
        return results;
    }

    @Override
    public JobStatus updateStatus(String id, JobStatus.Status status, String event, Integer processedCount, Integer errorCount) {
        synchronized (lock) {
            JobStatus result = null
            db.withTransaction {
                JobStatus item = getFromDb(db, id)
                if (item != null) {
                    updateStatusInDb(db, id, status ?: item.status, processedCount ?: 0, errorCount ?: 0, isCompleted(status) && !isCompleted(item.status))
                    result = getFromDb(db, id)
                }
            }
            return result
        }
    }

    private boolean isCompleted(JobStatus.Status status) {
        return status == JobStatus.Status.COMPLETED || status == JobStatus.Status.ERROR || status == JobStatus.Status.CANCELLED
    }

    @Override
    public JobStatus updateStatus(String id, JobStatus.Status status) {
        return updateStatus(id, status, null, 0, 0);
    }

    @Override
    public JobStatus updateStatus(String id, JobStatus.Status status, String event) {
        return updateStatus(id, status, event, 0, 0);
    }

    @Override
    public JobStatus incrementCounts(String id, int processedCount, int errorCount) {
        return updateStatus(id, null, null, processedCount, errorCount);
    }


}
