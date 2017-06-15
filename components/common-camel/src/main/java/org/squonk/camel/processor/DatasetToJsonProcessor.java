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

package org.squonk.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;

import java.io.InputStream;

/**
 * Created by timbo on 05/07/16.
 */
public class DatasetToJsonProcessor implements Processor {

    private final Class type;

    public DatasetToJsonProcessor(Class type) {
        this.type = type;
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        Dataset dataset = exchange.getIn().getBody(Dataset.class);
        if (dataset == null) {
            throw new IllegalStateException("Could not read Dataset. Should be present as the body.");
        }
        String acceptEncoding = exchange.getIn().getHeader("Accept-Encoding", String.class);
        boolean gzip = "gzip".equals(acceptEncoding);
        InputStream is = dataset.getInputStream(gzip);
        exchange.getIn().setBody(is);
        DatasetMetadata meta = dataset.getMetadata();
        if (meta != null) {
            String json = JsonHandler.getInstance().objectToJson(meta);
            exchange.getIn().setHeader(CamelCommonConstants.HEADER_METADATA, json);
        }
        if (gzip) {
            exchange.getIn().setHeader("Content-Encoding", "gzip");
        }
        exchange.getIn().setHeader("Content-Type", CommonMimeTypes.MIME_TYPE_DATASET_MOLECULE_JSON);
    }
}
