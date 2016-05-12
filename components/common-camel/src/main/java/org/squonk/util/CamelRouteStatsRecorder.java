package org.squonk.util;

import org.apache.camel.ProducerTemplate;

import java.util.Map;

/**
 * Created by timbo on 12/05/16.
 */
public class CamelRouteStatsRecorder extends StatsRecorder {

    private final ProducerTemplate pt;

    public CamelRouteStatsRecorder(String jobId, ProducerTemplate pt) {
        super(jobId);
        this.pt = pt;
    }

    @Override
    protected void sendStats(Map<String, Integer> executionStats) {
        super.sendStats(executionStats);
        pt.sendBodyAndHeader(executionStats, StatsRecorder.HEADER_SQUONK_JOB_ID, getJobId());
    }
}
