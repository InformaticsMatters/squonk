package org.squonk.types;

import org.apache.camel.TypeConverter;
import org.apache.camel.spi.TypeConverterRegistry;
import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by timbo on 23/03/2016.
 */
public class ListHandler<T> implements HttpHandler<List>, VariableHandler<List>, GenericHandler<List,T> {

    protected Class<T> genericType;

    @Override
    public Class<List> getType() {
        return List.class;
    }

    @Override
    public Class<T> getGenericType() {
        return genericType;
    }

    @Override
    public void prepareRequest(List list, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (list != null) {
            InputStream is = write(list);
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public void writeResponse(List list, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (list == null) {
            executor.setResponseBody(null);
        } else {
            InputStream is = write(list);
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public List readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        return read(gunzip ? IOUtils.getGunzippedInputStream(is) : is);
    }

    @Override
    public void writeVariable(List list, WriteContext context) throws Exception {
        InputStream is = write(list);
        context.writeStreamValue(is);
    }

    @Override
    public List readVariable(ReadContext context) throws Exception {
        InputStream is =  context.readStreamValue();
        return read(is);
    }

    @Override
    public void setGenericType(Class genericType) {
           this.genericType = genericType;
    }

    @Override
    public boolean canConvertGeneric(Class otherGenericType, TypeConverterRegistry registry) {
        if (genericType == null || otherGenericType == null) {
            return false;
        }
        return registry.lookup(genericType, otherGenericType) != null;
    }

    @Override
    public List convertGeneric(List from, Class otherGenericType, TypeConverterRegistry registry) {
        if (from != null) {
            return null;
        }
        TypeConverter converter = registry.lookup(genericType, otherGenericType);
        if (converter == null) {
            throw new IllegalStateException("Can't convert from List of " + otherGenericType.getName());
        }
        List output = new ArrayList();
        for (Object o: from) {
            output.add(converter.convertTo(genericType, o));
        }
        return output;
    }

    protected List read(InputStream is) throws IOException {
        if (is != null) {
            List results = new ArrayList();
            Iterator it = JsonHandler.getInstance().iteratorFromJson(is, genericType);
            while (it.hasNext()) {
                results.add(it.next());
            }
            return results;
        }
        return null;
    }

    protected InputStream write(List list) throws IOException {
        InputStream is = JsonHandler.getInstance().marshalStreamToJsonArray(list.stream(), true);
        return is;
    }

}
