package org.squonk.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by timbo on 11/05/2016.
 */
public class ExecutionStats {


    private Map<String, Integer> executionStats = new ConcurrentHashMap<>();


    public Map<String, Integer> getExecutionStats() {
        return executionStats;
    }

    public int incrementExecutionCount(String key, int count) {
        return increment(executionStats, key, count);
    }

    private int increment(Map<String, Integer> stats, String key, int count) {
        assert count >= 0;
        if (!stats.containsKey(key)) {
            stats.put(key, count);
            return count;
        } else {
            int i = stats.get(key) + count;
            stats.put(key, i);
            return i;
        }
    }
}