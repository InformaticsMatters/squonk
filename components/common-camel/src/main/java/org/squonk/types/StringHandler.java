package org.squonk.types;

import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.HttpExecutor;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class StringHandler implements HttpHandler<String>, VariableHandler<String> {

    @Override
    public void prepareRequest(String obj, HttpExecutor executor) throws IOException {
        if (obj != null) {
            executor.setRequestBody(new ByteArrayInputStream(obj.getBytes()));
        }
    }

    @Override
    public String readResponse(HttpExecutor executor) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return IOUtils.convertStreamToString(is);
        }
        return null;
    }

    @Override
    public void writeVariable(String value, WriteContext context) throws IOException {
        context.writeTextValue(value);
    }

    @Override
    public String readVariable(ReadContext context) throws IOException {
        return context.readTextValue();
    }
}
