package com.im.lac.services.job.dao;

import com.im.lac.job.jobdef.JobDefinition;
import com.im.lac.job.jobdef.JobQuery;
import com.im.lac.job.jobdef.JobStatus;
import org.squonk.client.JobStatusClient;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by timbo on 04/01/16.
 */
public class MemoryJobStatusClient implements JobStatusClient {

    private static final Logger LOG = Logger.getLogger(MemoryJobStatusClient.class.getName());

    public final static MemoryJobStatusClient INSTANCE = new MemoryJobStatusClient();

    private final Object lock = new Object();

    private final Map<String,JobStatus> store = new LinkedHashMap<>();

    @Override
    public JobStatus create(JobDefinition jobdef, String username, Integer totalCount) {
        LOG.finer("Registering JobDef: " + jobdef);
        JobStatus status = JobStatus.create(jobdef, username, new Date());
        store.put(status.getJobId(), status);
        return status;
    }

    @Override
    public JobStatus get(String id) {
        return store.get(id);
    }

    @Override
    public List<JobStatus> list(JobQuery query) {
        // TODO apply the filter
        List<JobStatus> results = new ArrayList<>();
        results.addAll(store.values());
        return results;
    }

    @Override
    public JobStatus updateStatus(String id, JobStatus.Status status, String event, Integer processedCount) {
        synchronized (lock) {
            JobStatus item = store.get(id);
            if (item == null) {
                throw new IllegalArgumentException("JobStatus with ID " + id + " not found");
            }
            JobStatus nue = item.withStatus(status == null ? item.getStatus() : status, processedCount, event);
            store.put(id, nue);
            return nue;
        }
    }

    @Override
    public JobStatus incrementProcesssedCount(String id, int count) {
        synchronized (lock) {
            JobStatus item = store.get(id);
            if (item == null) {
                throw new IllegalArgumentException("JobStatus with ID " + id + " not found");
            }
            JobStatus nue = item.withStatus(item.getStatus(), item.getProcessedCount() + count, null);
            store.put(id, nue);
            return nue;
        }
    }


}
