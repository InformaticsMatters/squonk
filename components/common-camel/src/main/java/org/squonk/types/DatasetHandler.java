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

package org.squonk.types;

import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.io.InputStreamDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.io.StringDataSource;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.ServiceConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 21/03/2016.
 */
public class DatasetHandler<T extends BasicObject> extends DefaultHandler<Dataset> {

    private static final Logger LOG = Logger.getLogger(DatasetHandler.class.getName());


    public DatasetHandler() {
        super(Dataset.class);
    }

    public DatasetHandler(Class<T> genericType) {
        super(Dataset.class, genericType);
    }

    @Override
    public void prepareRequest(Dataset dataset, RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) throws IOException {
        DatasetMetadata md = dataset.getMetadata();
        if (md != null) {
            String json = JsonHandler.getInstance().objectToJson(md);
            LOG.finer("Request metadata JSON: " + json);
            executor.prepareRequestHeader(ServiceConstants.HEADER_METADATA, json); // TODO - move this constant to this class
        }
        handleGzipHeaders(executor, gzipRequest, gzipResponse);
        InputStream is = dataset.getInputStream(gzipRequest);
        executor.prepareRequestBody(is);
    }

    @Override
    public Dataset<T> readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        String json = executor.getResponseHeader(CamelCommonConstants.HEADER_METADATA);
        InputStream is = executor.getResponseBody();
        if (is == null) {
            return null;
        }
        String mediaType = TypeDescriptor.resolveMediaType(Dataset.class, genericType);
        SquonkDataSource ds = new InputStreamDataSource(Dataset.ROLE_DATASET, SquonkDataSource.NAME_RESPONSE_BODY, mediaType, is, null);
        ds.setGzipContent(!gunzip);
        return create(json, ds);
    }

    @Override
    public void writeResponse(Dataset dataset, RequestResponseExecutor executor, boolean gzip) throws IOException {

        if (dataset == null) {
            executor.setResponseBody(null);
        } else {
            DatasetMetadata md = dataset.getMetadata();
            if (md != null) {
                String json = JsonHandler.getInstance().objectToJson(md);
                LOG.info("Metadata is present: " + json);
                executor.setResponseHeader(CamelCommonConstants.HEADER_METADATA, json); // TODO - move this constant to this class
            } else {
                LOG.warning("No metadata present");
            }
            if (gzip) {
                executor.setResponseHeader("Content-Encoding", "gzip");
            }
            // setting the header handles the compression
            executor.setResponseBody(dataset.getInputStream(false));
        }
    }

    private String resolveDatasetMediaType(Class secondaryType) {
        return TypeResolver.getInstance().resolveMediaType(Dataset.class, secondaryType);
    }

    private String resolveContentMediaType(Class secondaryType) {
        return TypeResolver.getInstance().resolveMediaType(secondaryType, null);
    }

    @Override
    public void writeVariable(Dataset dataset, WriteContext context) throws Exception {
        String mediaType = resolveContentMediaType(dataset.getType());
        Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
        Stream s = null;
        InputStream is = null;
        JsonHandler.MarshalData data = null;
        try {
            s = generator.getAsStream();
            data = generator.marshalData(s, false);
            is = data.getInputStream();
            context.writeStreamValue(is, mediaType, Dataset.ROLE_DATASET,null, true);
            try {
                data.getFuture().get();
            } catch (ExecutionException ex) { // reading Stream failed?
                //LOG.log(Level.SEVERE, "Writing failed", ex);
                try {
                    // clear the variable
                    context.deleteVariable();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to delete variable after error", e);
                }
                throw ex;
            }
        } finally {// stream now closed
            if (s != null ) { s.close(); }
            if (is != null ) { is.close(); }
        }

        DatasetMetadata md = generator.getDatasetMetadata();
        String json = JsonHandler.getInstance().objectToJson(md);
        context.writeTextValue(json, CommonMimeTypes.MIME_TYPE_DATASET_METADATA, Dataset.ROLE_METADATA);
    }

    @Override
    public Dataset<T> readVariable(ReadContext context) throws Exception {

        String json = context.readTextValue(CommonMimeTypes.MIME_TYPE_DATASET_METADATA, Dataset.ROLE_METADATA);
        DatasetMetadata<T> meta = createMetadata(json);
        String mediaType = resolveContentMediaType(meta.getType());
        SquonkDataSource data = context.readStreamValue(mediaType, Dataset.ROLE_DATASET);
        Dataset<T> result = new Dataset<>(data, meta);
        return result;
    }

    @Override
    public List<SquonkDataSource> readDataSources(ReadContext context) throws Exception {
        String json = context.readTextValue(CommonMimeTypes.MIME_TYPE_DATASET_METADATA, Dataset.ROLE_METADATA);
        // we have to parse out the dataset type from the metadata
        DatasetMetadata<T> meta = createMetadata(json);
        String mediaType = resolveContentMediaType(meta.getType());
        SquonkDataSource metaDs = new StringDataSource(Dataset.ROLE_METADATA, null, CommonMimeTypes.MIME_TYPE_DATASET_METADATA, json, false);
        SquonkDataSource dataDs = context.readStreamValue(mediaType, Dataset.ROLE_DATASET);
        List<SquonkDataSource> list = new ArrayList<>();
        list.add(metaDs);
        list.add(dataDs);
        return list;
    }

    private DatasetMetadata createMetadata(String json) throws IOException {
        DatasetMetadata<T> metadata = null;
        if (json != null) {
            //LOG.info("Metadata: " + meta);
            metadata = JsonHandler.getInstance().objectFromJson(json, DatasetMetadata.class);
        }
        if (metadata == null) {
            LOG.warning("No metadata present, using basic default");
            metadata = new DatasetMetadata<>(getGenericType());
        }
        return metadata;
    }

    protected Dataset<T> create(String meta, SquonkDataSource data) throws IOException {
        DatasetMetadata<T> metadata = createMetadata(meta);
        return new Dataset<>(data, metadata);
    }

    @Override
    public Dataset<T> create(SquonkDataSource data) throws Exception {
        return new Dataset(getGenericType(), data);
    }

    @Override
    public Dataset<T> create(List<SquonkDataSource> inputs) throws Exception {


        SquonkDataSource data = null;
        SquonkDataSource meta = null;
        for (SquonkDataSource input : inputs) {
            if (Dataset.ROLE_DATASET.equalsIgnoreCase(input.getRole())) {
                data = input;
            }
            if (Dataset.ROLE_METADATA.equalsIgnoreCase(input.getRole())) {
                meta = input;
            }
        }


        if (data == null) {
            throw new IllegalStateException("InputStream for data not present");
        }
        data.setGzipContent(true);
        if (meta == null) {
            return new Dataset<T>(getGenericType(), data);
        } else {
            meta.setGzipContent(false);
            DatasetMetadata<T> datasetMetadata = JsonHandler.getInstance().objectFromJson(meta.getInputStream(), DatasetMetadata.class);
            return new Dataset<T>(data, datasetMetadata);
        }
    }

    @Override
    protected Dataset<T> createMultiple(String mediaType, Class genericType, Map<String, InputStream> inputs) throws IOException {
        InputStream data = inputs.get(Dataset.ROLE_DATASET);
        InputStream meta = inputs.get(Dataset.ROLE_METADATA);
        if (data == null || meta == null) {
            throw new IllegalStateException("Inputs for data and metadata must be defined");
        }
        Dataset<T> result = new Dataset(data, meta);
        return result;
    }
}
