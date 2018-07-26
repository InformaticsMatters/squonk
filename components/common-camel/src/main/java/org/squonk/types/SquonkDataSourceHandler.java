/*
 * Copyright (c) 2018 Informatics Matters Ltd.
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

import org.squonk.http.RequestResponseExecutor;
import org.squonk.io.InputStreamDataSource;
import org.squonk.io.SquonkDataSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class SquonkDataSourceHandler extends DefaultHandler<SquonkDataSource> {

    private final String mediaType;
    private final String extension;
    private final String key;
    private final boolean gzip;


    public SquonkDataSourceHandler() {
        this(null, null, null, true);
    }

    public SquonkDataSourceHandler(String mediaType, String extension, String key, boolean gzip) {
        super(SquonkDataSource.class);
        this.mediaType = mediaType;
        this.extension = extension;
        this.key = key;
        this.gzip = gzip;
    }

    @Override
    public void prepareRequest(SquonkDataSource dataSource, RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) throws IOException {
        handleGzipHeaders(executor, gzipRequest, gzipResponse);
        if (dataSource != null) {
            if (dataSource.getContentType() != null) {
                executor.prepareRequestHeader("Content-Type", dataSource.getContentType());
            }
            dataSource.setGzipContent(gzipRequest);
            executor.prepareRequestBody(dataSource.getInputStream());
        }
    }

    @Override
    public void writeResponse(SquonkDataSource dataSource, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (dataSource == null) {
            executor.setResponseBody(null);
        }  else {
            dataSource.setGzipContent(gzip);
            executor.setResponseHeader("Content-Type", dataSource.getContentType());
            executor.setResponseHeader("filename", dataSource.getName());
            executor.setResponseBody(dataSource.getInputStream());
        }
    }

    @Override
    public SquonkDataSource readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            String encoding = executor.getResponseHeader("Content-Encoding");
            Boolean encoded = null;
            if (encoding != null && encoding.toLowerCase().contains("gzip")) {
                encoded = true;
            }
            String filename = executor.getResponseHeader("filename");
            SquonkDataSource dataSource = new InputStreamDataSource(filename, executor.getResponseHeader("Content-Type"), is, encoded);
            dataSource.setGzipContent(!gunzip);
            return dataSource;
        }
        return null;
    }

    @Override
    public void writeVariable(SquonkDataSource value, WriteContext context) throws Exception {
        context.writeStreamValue(value.getInputStream(), mediaType, extension, key, gzip);
    }

    @Override
    public SquonkDataSource readVariable(ReadContext context) throws Exception {
        SquonkDataSource ds = context.readStreamValue(mediaType, extension, key);
        return ds;
    }

}