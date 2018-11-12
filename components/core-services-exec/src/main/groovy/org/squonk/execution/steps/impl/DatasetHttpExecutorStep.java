/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

package org.squonk.execution.steps.impl;

import org.apache.camel.CamelContext;
import org.squonk.dataset.Dataset;
import org.squonk.types.BasicObject;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.StatsRecorder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** Thin executor sends only the molecules (no values) to the service and combines the returned values with the
 * originals. As such the network traffic is minimised and the remote end does not need to handle values which it may
 * not be able to represent.
 * Whether the resulting structure is the input structure or the one returned by the service is determined by the
 * OPTION_PRESERVE_STRUCTURE option.
 *
 * NOTE: the input is held in memory until the corresponding molecule is returned from the service which usually means that
 * the large datasets will be handled OK, but in some cases there could be issues. Examples include when results are returned
 * out of order or when only a subset of the input molecules are returned.
 *
 * TODO - make this support generic fully so that it can handle things other than MoleculeObjects
 *
 *
 * @author timbo
 */
public class DatasetHttpExecutorStep extends AbstractDatasetStep {

    private static final Logger LOG = Logger.getLogger(DatasetHttpExecutorStep.class.getName());

    public DatasetHttpExecutorStep() {
        enableThinExecution = true;
    }

    @Override
    protected Dataset doExecuteWithDataset(Dataset input, CamelContext camelContext) throws Exception {

        String endpoint = getHttpExecutionEndpoint();

        Map<String, Object> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept-Encoding", "gzip");
        // NOTE: setting the Content-Encoding will cause camel to gzip the data, we don't need to do it
        requestHeaders.put("Content-Encoding", "gzip");
        if (jobId != null) {
            requestHeaders.put(StatsRecorder.HEADER_SQUONK_JOB_ID, jobId);
        }

        Stream<? extends BasicObject> stream1 = input.getStream();
        final AtomicInteger count = new AtomicInteger(0);
        stream1 = stream1.peek((o) -> {
            count.incrementAndGet();
        }).onClose(() -> {
            numProcessed = count.intValue();
        });

        InputStream data = JsonHandler.getInstance().marshalStreamToJsonArray(stream1, false);

        Dataset<? extends BasicObject> results = handleHttpPost(camelContext, endpoint, data, requestHeaders);
        return results;
    }
}
