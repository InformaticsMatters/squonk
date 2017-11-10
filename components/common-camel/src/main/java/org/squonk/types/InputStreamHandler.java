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

import org.squonk.api.HttpHandler;
import org.squonk.api.VariableHandler;
import org.squonk.http.RequestResponseExecutor;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class InputStreamHandler implements HttpHandler<InputStream>, VariableHandler<InputStream> {

    private final String mediaType;
    private final String extension;
    private final String key;
    private final boolean gzip;


    public InputStreamHandler() {
        mediaType = null;
        extension = null;
        key = null;
        gzip = true;
    }

    public InputStreamHandler(String mediaType, String extension, String key, boolean gzip) {
        this.mediaType = mediaType;
        this.extension = extension;
        this.key = key;
        this.gzip = gzip;
    }

    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public void prepareRequest(InputStream is, RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) throws IOException {
        handleGzipHeaders(executor, gzipRequest, gzipResponse);
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
        context.writeStreamValue(value, mediaType, extension, key, gzip);
    }

    @Override
    public InputStream readVariable(ReadContext context) throws Exception {
        return context.readStreamValue(mediaType, extension, key);
    }
}