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
public class SDFileHandler implements HttpHandler<SDFile>, VariableHandler<SDFile> {

    @Override
    public void prepareRequest(SDFile sdf, HttpExecutor executor) throws IOException {
        if (sdf != null) {
            executor.setRequestBody(sdf.getInputStream());
        }
    }

    @Override
    public SDFile readResponse(HttpExecutor executor) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return new SDFile(is);
        }
        return null;
    }

    @Override
    public void writeVariable(SDFile sdf, WriteContext context) throws IOException {
        context.writeStreamValue(IOUtils.getGzippedInputStream(sdf.getInputStream()));
    }

    @Override
    public SDFile readVariable(ReadContext context) throws IOException {
        InputStream is =  context.readStreamValue();
        return new SDFile(is);
    }
}
