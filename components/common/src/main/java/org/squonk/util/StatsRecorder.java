package org.squonk.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 11/05/2016.
 */
public class StatsRecorder {

    public static final String HEADER_SQUONK_JOB_ID = "SquonkJobID";
    public static final String HEADER_STATS_RECORDER = "StatsRecorder";


    private static final Logger LOG = Logger.getLogger(StatsRecorder.class.getName());

    protected final String jobId;

    public StatsRecorder(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void recordStats(String key,Integer count) {
        recordStats(Collections.singletonMap(key, count));
    }

    public void recordStats(Map<String,Integer> stats) {
        recordStats(Collections.singletonList(stats));
    }

    public void recordStats(List<Map<String,Integer>> stats) {
        Map<String,Integer> accumulatedExecutionStats = new HashMap<>();
        for (Map<String,Integer> stat : stats) {
                updateStats(accumulatedExecutionStats, stat);
        }
        sendStats(new ExecutionStats(jobId, accumulatedExecutionStats));
    }

    private void updateStats(Map<String,Integer> accumulated, Map<String,Integer> toAdd) {
        for (Map.Entry<String,Integer>  e : toAdd.entrySet()) {
            String k = e.getKey();
            if (accumulated.containsKey(k)) {
                int total = accumulated.get(k) + e.getValue();
                accumulated.put(k, total);
            } else {
                accumulated.put(k, e.getValue());
            }
        }
    }


    /** Default implementation just logs the stats. Override to do something more useful
     *
     * @param executionStats
     */
    protected void sendStats(ExecutionStats executionStats) {
        LOG.info(executionStats.toString());
    }

}
