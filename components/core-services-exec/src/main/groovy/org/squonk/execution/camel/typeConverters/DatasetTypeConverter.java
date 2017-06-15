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

package org.squonk.execution.camel.typeConverters;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.squonk.core.client.StructureIOClient;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.MoleculeObjectDataset;
import org.squonk.types.MoleculeObject;
import org.squonk.types.SDFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 26/02/17.
 */
@Converter
public class DatasetTypeConverter {

    @Converter
    public static SDFile convertDatasetToSDFile(Dataset<MoleculeObject> dataset, Exchange exchange) throws IOException {
        if (dataset.getType() == MoleculeObject.class) {
            return convertMoleculeObjectDatasetToSDFile(new MoleculeObjectDataset(dataset), exchange);
        } else {
            return null;
        }
    }

    @Converter
    public static SDFile convertMoleculeObjectDatasetToSDFile(MoleculeObjectDataset dataset, Exchange exchange) throws IOException {
        InputStream is = StructureIOClient.CDK.datasetExportToSdf(dataset.getDataset(), false);
        SDFile sdf = new SDFile(is);
        return sdf;
    }
}
