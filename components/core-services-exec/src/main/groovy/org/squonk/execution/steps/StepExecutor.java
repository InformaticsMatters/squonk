package org.squonk.execution.steps;

import org.apache.camel.ProducerTemplate;
import org.squonk.execution.variable.VariableManager;
import org.apache.camel.CamelContext;
import org.squonk.util.CamelRouteStatsRecorder;
import org.squonk.util.StatsRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author timbo
 */
public class StepExecutor {

    private final VariableManager varman;
    private final Long producer;
    private final String jobId;
    private final String statsRoute;
    private volatile Step currentStep;

    public StepExecutor(Long producer, String jobId, VariableManager varman) {
        this(producer, jobId, varman, null);
    }

    public StepExecutor(Long producer, String jobId, VariableManager varman, String statsRoute) {
        this.producer = producer;
        this.jobId = jobId;
        this.varman = varman;
        this.statsRoute = statsRoute;
    }

    public void execute(StepDefinition[] defs, CamelContext context) throws Exception {
        Step[] steps = new Step[defs.length];
        for (int i = 0; i < defs.length; i++) {
            StepDefinition def = defs[i];
            Class cls = Class.forName(def.getImplementationClass());
            Step step = (Step) cls.newInstance();
            step.configure(producer, jobId, def.getOptions(), def.getInputVariableMappings(), def.getOutputVariableMappings());
            steps[i] = step;
        }
        execute(steps, context);
    }

    public void execute(Step[] steps, CamelContext context) throws Exception {

        StringBuilder b = new StringBuilder("Executing steps:\n");
        for (Step step : steps) {
            b.append(step.toString()).append("\n");
        }
        System.out.println(b.toString());


        List<Map<String, Integer>> statsList = new ArrayList<>();
        for (Step step : steps) {
            currentStep = step;
            step.execute(varman, context);
            Map<String, Integer> stats = step.getUsageStats();
            if (stats.size() > 0) {
                statsList.add(stats);
            }
        }
        if (statsRoute != null && statsList.size() > 0) {
            // send stats
            ProducerTemplate pt = context.createProducerTemplate();
            pt.setDefaultEndpointUri(statsRoute);
            StatsRecorder recorder = new CamelRouteStatsRecorder(jobId, pt);
            recorder.recordStats(statsList);
        }
    }

    public String getCurrentStatus() {
        Step s = currentStep;
        if (s != null) {
            return s.getStatusMessage();
        }
        return null;
    }

}
