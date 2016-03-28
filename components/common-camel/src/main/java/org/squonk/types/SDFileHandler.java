package org.squonk.types;

import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class SDFileHandler implements HttpHandler<SDFile>, VariableHandler<SDFile> {

    @Override
    public Class<SDFile> getType() {
        return SDFile.class;
    }

    @Override
    public void prepareRequest(SDFile sdf, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (sdf != null) {
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(sdf.getInputStream()) : sdf.getInputStream());
        }
    }

    @Override
    public void writeResponse(SDFile sdf, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (sdf == null) {
            executor.setResponseBody(null);
        } else {
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(sdf.getInputStream()) : sdf.getInputStream());
        }
    }

    @Override
    public SDFile readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return new SDFile(gunzip ? IOUtils.getGunzippedInputStream(is) : is);
        }
        return null;
    }

    @Override
    public void writeVariable(SDFile sdf, WriteContext context) throws IOException {
        context.writeStreamValue(sdf.getInputStream());
    }

    @Override
    public SDFile readVariable(ReadContext context) throws IOException {
        InputStream is =  context.readStreamValue();
        return new SDFile(is);
    }
}
