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

package org.squonk.types;

import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;
import org.squonk.util.ServiceConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by timbo on 21/03/2016.
 */
public class DatasetHandler<T extends BasicObject> implements VariableHandler<Dataset>, HttpHandler<Dataset>, GenericHandler<Dataset,T> {

    private static final Logger LOG = Logger.getLogger(DatasetHandler.class.getName());
    private Class<T> genericType;

    /** Default constructor.
     * If using this constructor the generic type MUST be set using the {@link #setGenericType(Class)} method
     *
     */
    public DatasetHandler() { }

    public DatasetHandler(Class<T> genericType) {
        this.genericType = genericType;
    }

    @Override
    public Class<Dataset> getType() {
        return Dataset.class;
    }

    @Override
    public Class<T> getGenericType() {
        return genericType;
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
        return create(json, gunzip ? IOUtils.getGunzippedInputStream(is) : is);
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

    private String resolveMediaType(Class secondaryType) {
        return TypeResolver.getInstance().resolveMediaType(Dataset.class, secondaryType);
    }

    @Override
    public void writeVariable(Dataset dataset, WriteContext context) throws Exception {
        String mediaType = resolveMediaType(dataset.getType());
        Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
        Stream s = null;
        InputStream is = null;
        JsonHandler.MarshalData data = null;
        try {
            s = generator.getAsStream();
            data = generator.marshalData(s, false);
            is = data.getInputStream();
            context.writeStreamValue(is, mediaType, Dataset.DATASET_FILE_EXT,null, true);
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
        context.writeTextValue(json, CommonMimeTypes.MIME_TYPE_DATASET_METADATA, Dataset.METADATA_FILE_EXT);
    }

    @Override
    public Dataset<T> readVariable(ReadContext context) throws Exception {

        String json = context.readTextValue(CommonMimeTypes.MIME_TYPE_DATASET_METADATA, Dataset.METADATA_FILE_EXT);
        DatasetMetadata<T> meta = createMetadata(json);
        String mediaType = resolveMediaType(meta.getType());
        InputStream data = context.readStreamValue(mediaType, Dataset.DATASET_FILE_EXT);
        Dataset<T> result = new Dataset<>(data, meta);
        return result;
    }

    private DatasetMetadata createMetadata(String json) throws IOException {
        DatasetMetadata<T> metadata = null;
        if (json != null) {
            //LOG.info("Metadata: " + meta);
            metadata = JsonHandler.getInstance().objectFromJson(json, DatasetMetadata.class);
        }
        if (metadata == null) {
            LOG.warning("No metadata present, using basic default");
            metadata = new DatasetMetadata<>(genericType);
        }
        return metadata;
    }

    protected Dataset<T> create(String meta, InputStream data) throws IOException {
        DatasetMetadata<T> metadata = createMetadata(meta);
        return new Dataset<>(data, metadata);
    }

    @Override
    public void setGenericType(Class<T> genericType) {
        this.genericType = genericType;
    }
}
