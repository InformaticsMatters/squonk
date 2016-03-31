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
public class InputStreamHandler implements HttpHandler<InputStream>, VariableHandler<InputStream> {

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public void prepareRequest(InputStream is, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (is != null) {
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public void writeResponse(InputStream is, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (is == null) {
            executor.setResponseBody(null);
        }  else {
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public InputStream readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return gunzip ? IOUtils.getGunzippedInputStream(is) : is;
        }
        return null;
    }

    @Override
    public void writeVariable(InputStream value, WriteContext context) throws Exception {
        context.writeStreamValue(value);
    }

    @Override
    public InputStream readVariable(ReadContext context) throws Exception {
        return context.readStreamValue();
    }
}
