package org.squonk.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by timbo on 11/05/2016.
 */
public class StatsRecorder {

    protected final String jobId;

    public StatsRecorder(String jobId) {
        this.jobId = jobId;
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


    private void sendStats( Map<String,Integer> executionStats) {

        for (Map.Entry<String,Integer> e : executionStats.entrySet()) {
            // TODO - send somewhere
            System.out.println("STATS: " + jobId + ": " + e.getKey() + " -> " + e.getValue());
        }
    }

}
