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

package org.squonk.camel.cdk.processor;

import org.apache.camel.Exchange;
import org.squonk.api.HttpHandler;
import org.squonk.camel.processor.AbstractMoleculeObjectHttpProcessor;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.http.CamelRequestResponseExecutor;
import org.squonk.http.RequestInfo;
import org.squonk.types.CDKSDFile;
import org.squonk.types.TypeResolver;

import java.io.IOException;

import static org.squonk.api.MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON;
import static org.squonk.api.MimeTypeResolver.MIME_TYPE_MDL_SDF;

/**
 * Created by timbo on 30/03/16.
 */
public class CDKMoleculeObjectSDFileProcessor extends AbstractMoleculeObjectHttpProcessor {

    public static final String[] INPUT_MIME_TYPES = new String[] {MIME_TYPE_DATASET_MOLECULE_JSON};
    public static final String[] OUTPUT_MIME_TYPES = new String[] {MIME_TYPE_MDL_SDF};


    public CDKMoleculeObjectSDFileProcessor(TypeResolver resolver) {
        super(resolver, INPUT_MIME_TYPES, OUTPUT_MIME_TYPES);
    }

    @Override
    protected Object processDataset(Exchange exch, MoleculeObjectDataset dataset, RequestInfo requestInfo) throws IOException {
        return new CDKSDFile(dataset.getDataset().getInputStream(false));
    }
}
