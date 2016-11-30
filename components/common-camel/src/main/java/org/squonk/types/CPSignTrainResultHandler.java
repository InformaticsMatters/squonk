package org.squonk.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class CPSignTrainResultHandler implements HttpHandler<CPSignTrainResult>, VariableHandler<CPSignTrainResult> {

    @Override
    public Class<CPSignTrainResult> getType() {
        return CPSignTrainResult.class;
    }

    @Override
    public void prepareRequest(CPSignTrainResult result, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (result != null) {
            InputStream is = convertToJsonInputStream(result);
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public void writeResponse(CPSignTrainResult result, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (result == null) {
            executor.setResponseBody(null);
        } else {
            InputStream is = convertToJsonInputStream(result);
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public CPSignTrainResult readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        return convertFromJsonInputStream(executor.getResponseBody(), gunzip);
    }

    @Override
    public void writeVariable(CPSignTrainResult result, WriteContext context) throws Exception {
        InputStream is = convertToJsonInputStream(result);
        context.writeStreamValue(is);
    }

    @Override
    public CPSignTrainResult readVariable(ReadContext context) throws Exception {
        return convertFromJsonInputStream(context.readStreamValue(), false);
    }

    private InputStream convertToJsonInputStream(CPSignTrainResult result) throws JsonProcessingException {
        byte[] json = JsonHandler.getInstance().objectToBytes(result);
        System.out.println("CPSignTrainResult: " + new String(json));
        return new ByteArrayInputStream(json);
    }

    private CPSignTrainResult convertFromJsonInputStream(InputStream is, boolean gunzip) throws IOException {
        if (is == null) {
            return null;
        } else {
            return JsonHandler.getInstance().objectFromJson(gunzip ? new GZIPInputStream(is) : is, CPSignTrainResult.class);
        }
    }
}
