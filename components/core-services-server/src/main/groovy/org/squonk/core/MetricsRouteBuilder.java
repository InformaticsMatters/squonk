package org.squonk.core;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.squonk.core.service.metrics.TokensPostgresClient;
import org.squonk.jobdef.StepsCellExecutorJobDefinition;
import org.squonk.mqueue.MessageQueueCredentials;
import org.squonk.util.ExecutionStats;
import org.squonk.util.StatsRecorder;

import java.util.Map;
import java.util.logging.Logger;

import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_NAME;
import static org.squonk.mqueue.MessageQueueCredentials.MQUEUE_JOB_METRICS_EXCHANGE_PARAMS;

/**
 * Created by timbo on 23/06/16.
 */
public class MetricsRouteBuilder extends RouteBuilder {

    private static final Logger LOG = Logger.getLogger(MetricsRouteBuilder.class.getName());

    private final MessageQueueCredentials rabbitmqCredentials = new MessageQueueCredentials();
    private final TokensPostgresClient client = new TokensPostgresClient();


    @Override
    public void configure() throws Exception {

        String mqueue = rabbitmqCredentials.generateUrl(MQUEUE_JOB_METRICS_EXCHANGE_NAME, MQUEUE_JOB_METRICS_EXCHANGE_PARAMS) +
                "&routingKey=tokens.#&concurrentConsumers=5";

        // TODO - handle exceptions

        LOG.info("Starting to consume from " + mqueue);
        from(mqueue)
                .log("consumed message ${body}")
                .unmarshal().json(JsonLibrary.Jackson, ExecutionStats.class)
                .log("TOKENS: ${body}")
                .bean(client, "saveExecutionStats")
//                .process((Exchange exch) -> {
//                    ExecutionStats stats = exch.getIn().getBody(ExecutionStats.class);
//                    if (stats != null) {
//                        LOG.info(stats.toString());
//                        // TODO - process the token usage e.g write to database
//                        client.saveExecutionStats();
//                    }
//                })
                 ;

    }
}
