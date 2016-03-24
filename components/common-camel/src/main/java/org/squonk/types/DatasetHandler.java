package org.squonk.types;

import com.im.lac.types.BasicObject;
import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.camel.CamelCommonConstants;
import org.squonk.dataset.Dataset;
import org.squonk.dataset.DatasetMetadata;
import org.squonk.http.HttpExecutor;
import org.squonk.types.io.JsonHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Created by timbo on 21/03/2016.
 */
public class DatasetHandler<T extends BasicObject> implements VariableHandler<Dataset>, HttpHandler<Dataset>, GenericHandler<Dataset,T> {

    private Class<T> genericType;

    /** Default constructor. If using this the generic type MUST be set using the {@link #setGenericType(Class)} method
     *
     */
    public DatasetHandler() { }

    public DatasetHandler(Class<T> genericType) {
        this.genericType = genericType;
    }

    @Override
    public void prepareRequest(Dataset dataset, HttpExecutor executor) throws IOException {
        DatasetMetadata md = dataset.getMetadata();
        String json = JsonHandler.getInstance().objectToJson(md);
        executor.addRequestHeader(CamelCommonConstants.HEADER_METADATA, json); // TODO - move this constant to this class
        executor.setRequestBody(dataset.getInputStream(false)); // context should work out whether to gzip
    }

    @Override
    public Dataset readResponse(HttpExecutor executor) throws IOException {
        String json = executor.getResponseHeader(CamelCommonConstants.HEADER_METADATA);
        InputStream is = executor.getResponseBody();
        return create(json, is);
    }

    @Override
    public void writeVariable(Dataset dataset, WriteContext context) throws IOException {
        Dataset.DatasetMetadataGenerator generator = dataset.createDatasetMetadataGenerator();
        try (Stream s = generator.getAsStream()) {
            InputStream is = generator.getAsInputStream(s, true);
            context.writeStreamValue(is);
        } // stream now closed
        DatasetMetadata md = generator.getDatasetMetadata();
        String json = JsonHandler.getInstance().objectToJson(md);
        context.writeTextValue(json);
    }

    @Override
    public Dataset<T> readVariable(ReadContext context) throws IOException {
        return create(context.readTextValue(), context.readStreamValue());
    }

    protected Dataset<T> create(String meta, InputStream data) throws IOException {
        DatasetMetadata<T> metadata = null;
        if (meta != null) {
            metadata = JsonHandler.getInstance().objectFromJson(meta, DatasetMetadata.class);
        }
        if (metadata == null) {
            metadata = new DatasetMetadata<>(genericType);
        }
        return new Dataset<>(metadata.getType(), data, metadata);
    }

    @Override
    public void setGenericType(Class<T> genericType) {
        this.genericType = genericType;
    }
}
