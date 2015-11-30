package com.im.lac.job.jobdef;

import java.util.Date;
import java.util.List;

/**
 *
 * @author timbo
 */
public class JobQuery {

    private final Integer max;
    private final List<JobStatus> status;
    private final Date submissionTimeStart;
    private final Date submissionTimeEnd;
    private final Date completionTimeStart;
    private final Date completionTimeEnd;



    public JobQuery(
            Integer max,
            List<JobStatus> status,
            Date submissionTimeStart,
            Date submissionTimeEnd,
            Date completionTimeStart,
            Date completionTimeEnd) {
        this.max = max;
        this.status = status;
        this.submissionTimeStart = submissionTimeStart;
        this.submissionTimeEnd = submissionTimeEnd;
        this.completionTimeStart = completionTimeStart;
        this.completionTimeEnd = completionTimeEnd;
    }

    public Integer getMax() {
        return max;
    }

    public List<JobStatus> getStatus() {
        return status;
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
