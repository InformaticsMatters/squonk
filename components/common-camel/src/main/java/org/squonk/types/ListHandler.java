package org.squonk.types;

import org.apache.camel.TypeConverter;
import org.apache.camel.spi.TypeConverterRegistry;
import org.squonk.api.GenericHandler;
import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.HttpExecutor;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class ListHandler<T> implements HttpHandler<List>, VariableHandler<List>, GenericHandler<List,T> {

    protected Class<T> genericType;

    @Override
    public void prepareRequest(List list, HttpExecutor executor) throws IOException {
        if (list != null) {
            InputStream is = write(list);
            executor.setRequestBody(is);
        }
    }

    @Override
    public List readResponse(HttpExecutor executor) throws IOException {
        InputStream is = executor.getResponseBody();
        return read(is);
    }

    @Override
    public void writeVariable(List list, WriteContext context) throws IOException {
        InputStream is = write(list);
        context.writeStreamValue(is);
    }

    @Override
    public List readVariable(ReadContext context) throws IOException {
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
            InputStream gunzipped = IOUtils.getGunzippedInputStream(is);
            Iterator it = JsonHandler.getInstance().iteratorFromJson(gunzipped, genericType);
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
