package com.im.lac.job.jobdef;

import java.util.Date;
import java.util.List;

/**
 *
 * @author timbo
 */
public class JobQuery {

    private final String username;
    private final Integer max;
    private final List<JobStatus> statuses;
    private final Date submissionTimeStart;
    private final Date submissionTimeEnd;
    private final Date completionTimeStart;
    private final Date completionTimeEnd;


    public JobQuery() {
        this(null, null, null, null, null, null, null);
    }

    public JobQuery(
            String username,
            Integer max,
            List<JobStatus> statuses,
            Date submissionTimeStart,
            Date submissionTimeEnd,
            Date completionTimeStart,
            Date completionTimeEnd) {
        this.username = username;
        this.max = max;
        this.statuses = statuses;
        this.submissionTimeStart = submissionTimeStart;
        this.submissionTimeEnd = submissionTimeEnd;
        this.completionTimeStart = completionTimeStart;
        this.completionTimeEnd = completionTimeEnd;
    }

    public String getUsername() {
        return username;
    }

    public Integer getMax() {
        return max;
    }

    public List<JobStatus> getStatuses() {
        return statuses;
    }

    public Date getSubmissionTimeStart() {
        return submissionTimeStart;
    }

    public Date getSubmissionTimeEnd() {
        return submissionTimeEnd;
    }

    public Date getCompletionTimeStart() {
        return completionTimeStart;
    }

    public Date getCompletionTimeEnd() {
        return completionTimeEnd;
    }

}
