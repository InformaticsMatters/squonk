/*
 * Copyright (c) 2017 Informatics Matters Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                .bean(client, "saveExecutionStats");

    }
}
