package org.squonk.types;

import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class StringHandler implements HttpHandler<String>, VariableHandler<String> {

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public void prepareRequest(String s, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (s != null) {
            InputStream is = new ByteArrayInputStream(s.getBytes());
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public void writeResponse(String s, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (s == null) {
            executor.setResponseBody(null);
        }  else {
            InputStream is = new ByteArrayInputStream(s.getBytes());
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public String readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return IOUtils.convertStreamToString(gunzip ? IOUtils.getGunzippedInputStream(is) : is);
        }
        return null;
    }

    @Override
    public void writeVariable(String value, WriteContext context) throws Exception {
        context.writeTextValue(value);
    }

    @Override
    public String readVariable(ReadContext context) throws Exception {
        return context.readTextValue();
    }
}
