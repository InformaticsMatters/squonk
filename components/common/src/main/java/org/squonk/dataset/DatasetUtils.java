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

package org.squonk.dataset;

import org.squonk.types.io.JsonHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by timbo on 08/03/16.
 */
public class DatasetUtils {

    private static final Logger LOG = Logger.getLogger(DatasetUtils.class.getName());

    /**
     * Merges multiple metadatas. The value class mappings and properties are combined with later values replacing earlier ones.
     * The size is retained if all sizes are the same, otherwise size is set to zero.
     * Dataset and field properties are merged according to the behavior of the corresponding mergeDatasetProperty() and
     * mergeFieldProperty() methods of DatasetMetadata.
     * The resulting metadata is of the type specified by the first item. Subsequent items do not have to be of the same type.
     *
     * @param meta
     * @return
     */
    public static DatasetMetadata mergeDatasetMetadata(DatasetMetadata... meta) {

        if (meta == null || meta.length == 0) {
            return null;
        } else if (meta.length == 1) {
            return meta[0];
        } else {
            DatasetMetadata result = new DatasetMetadata(meta[0].getType(), null, -1);
            for (int i=0; i<meta.length; i++) {
                if (meta[i].getType() != result.getType()) {
                    LOG.info("Merging metadata of type " + result.getType() + " with type " + meta[i].getType());
                }
                result.getValueClassMappings().putAll(meta[i].getValueClassMappings());
                if (result.getSize() != meta[i].getSize()) {
                    // sizes are inconsistent to set to zero to say that we don't know
                    result.setSize(0);
                }

                // merge dataset props
                Map<String,Object> datasetProps = meta[i].getProperties();
                for (Map.Entry<String,Object> e: datasetProps.entrySet()) {
                    result.mergeDatasetProperty(e.getKey(), e.getValue());
                }

                // merge field props
                List<DatasetMetadata.PropertiesHolder> phs = meta[i].getFieldMetaProps();
                for (DatasetMetadata.PropertiesHolder ph : phs) {
                    for (Map.Entry<String,Object> e: ph.getValues().entrySet()) {
                        result.mergeFieldProperty(ph.getFieldName(), e.getKey(), e.getValue());
                    }
                }
            }

            return result;
        }
    }

    public static ThinDatasetWrapper createThinDatasetWrapper(ThinDescriptor td, Class type, Map<String,Object> options) {
        Boolean isFiltering = td.isFiltering();
        Boolean isPreserve = td.isPreserve();

        return new ThinDatasetWrapper(type,
                isFiltering == null ? false: isFiltering, // null means false - the service does not filter
                isPreserve == null ? true : isPreserve,   // null means true - the service does not modify the core details and might return BasicObjects
                td.getFieldDescriptors(), options);
    }

    /** Helper method to create a dataset from its JSON and metadata
     *
     * @param data InputStream with the JSON with the data
     * @param metadata InputStream with the JSON with the metadata
     * @return
     * @throws IOException
     */
    public static Dataset createDataset(InputStream data, InputStream metadata) throws IOException {
        DatasetMetadata meta = JsonHandler.getInstance().objectFromJson(metadata, DatasetMetadata.class);
        return new Dataset(data, meta);
    }
}
