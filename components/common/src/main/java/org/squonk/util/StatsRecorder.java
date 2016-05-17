package org.squonk.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by timbo on 11/05/2016.
 */
public class StatsRecorder {

    public static final String HEADER_SQUONK_JOB_ID = "SquonkJobID";



    private static final Logger LOG = Logger.getLogger(StatsRecorder.class.getName());

    protected final String jobId;

    public StatsRecorder(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void recordStats(ExecutionStats stats) {
        recordStats(Collections.singletonList(stats));
    }

    public void recordStats(List<ExecutionStats> stats) {
        Map<String,Integer> accumulatedExecutionStats = new HashMap<>();
        for (ExecutionStats stat : stats) {
            if (stat != null) {
                updateStats(accumulatedExecutionStats, stat.getExecutionStats());
            }
        }
        sendStats(accumulatedExecutionStats);
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
    protected void sendStats( Map<String,Integer> executionStats) {
        LOG.info("STATS: " + jobId + ": " + executionStats.entrySet().stream().map((e) -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(",")));
    }

}
