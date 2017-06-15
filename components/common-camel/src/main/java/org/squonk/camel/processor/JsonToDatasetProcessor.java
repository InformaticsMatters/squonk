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
import org.squonk.util.IOUtils;

import java.io.InputStream;

/**
 * Created by timbo on 05/07/16.
 */
public class JsonToDatasetProcessor implements Processor {

    private final Class type;

    public JsonToDatasetProcessor(Class type) {
        this.type = type;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        InputStream is = exchange.getIn().getBody(InputStream.class);
        if (is == null) {
            throw new IllegalStateException("Could not read JSON. Should be present as the body.");
        }
        String metaJson = exchange.getIn().getHeader(CamelCommonConstants.HEADER_METADATA, String.class);
        DatasetMetadata meta = metaJson == null ? new DatasetMetadata(type) : JsonHandler.getInstance().objectFromJson(metaJson, DatasetMetadata.class);
        is = IOUtils.getGunzippedInputStream(is);
        Dataset dataset = new Dataset(type, is, meta);
        exchange.getIn().setBody(dataset);
    }
}
