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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.types.io.JsonHandler;
import org.squonk.util.CommonMimeTypes;
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
    public void prepareRequest(CPSignTrainResult result, RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) throws IOException {
        if (result == null) {
            throw new NullPointerException("CPSignTrainResult cannot be null");
        }
        handleGzipHeaders(executor, gzipRequest, gzipResponse);
        InputStream is = convertToJsonInputStream(result);
        executor.prepareRequestBody(gzipRequest ? IOUtils.getGzippedInputStream(is) : is);
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
        context.writeStreamValue(is, CommonMimeTypes.MIME_TYPE_CPSIGN_TRAIN_RESULT, "cpsign", null, false);
    }

    @Override
    public CPSignTrainResult readVariable(ReadContext context) throws Exception {
        return convertFromJsonInputStream(
                context.readStreamValue(CommonMimeTypes.MIME_TYPE_CPSIGN_TRAIN_RESULT, "cpsign", null),
                false);
    }

    private InputStream convertToJsonInputStream(CPSignTrainResult result) throws JsonProcessingException {
        byte[] json = JsonHandler.getInstance().objectToBytes(result);
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
