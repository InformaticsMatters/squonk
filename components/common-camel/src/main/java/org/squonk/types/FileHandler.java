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

import org.squonk.http.RequestResponseExecutor;
import org.squonk.io.InputStreamDataSource;
import org.squonk.io.SquonkDataSource;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/** Generic handler for files that handle their data as an InputStream. Should cover files of any format.
 * To use this class create a concrete subclass and just call the constructor.
 *
 * Created by timbo on 23/03/2016.
 */
abstract class FileHandler<T extends AbstractStreamType> extends DefaultHandler<T> {

    private static final Logger LOG = Logger.getLogger(FileHandler.class.getName());

    private final String mediaType;
    private final String extension;
    private final boolean defaultGzip;

    FileHandler(Class<T> type, String mediaType, String extension, boolean defaultGzip) {
        super(type);
        this.mediaType = mediaType;
        this.extension = extension;
        this.defaultGzip = defaultGzip;
    }

    public boolean isDefaultGzip() {
        return defaultGzip;
    }

    @Override
    public void prepareRequest(T file, RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) throws IOException {
        if (file == null) {
            throw new NullPointerException("File object cannot be null");
        }
        handleGzipHeaders(executor, gzipRequest, gzipResponse);
        executor.prepareRequestBody(gzipRequest ? IOUtils.getGzippedInputStream(file.getInputStream()) : file.getInputStream());
    }

    @Override
    public void writeResponse(T file, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (file == null) {
            executor.setResponseBody(null);
        } else {
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(file.getInputStream()) : file.getInputStream());
        }
    }

    @Override
    public T readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            try {
                SquonkDataSource ds = new InputStreamDataSource("", mediaType, is, null);
                ds.setGzipContent(!gunzip);
                return create(ds);
            } catch (Exception e) {
                throw new IOException("Failed to create instance of " + getType().getName(), e);
            }
        }
        return null;
    }

    @Override
    public void writeVariable(T file, WriteContext context) throws Exception {
        context.writeStreamValue(file.getInputStream(), mediaType, extension, null, isDefaultGzip());
    }

    @Override
    public T readVariable(ReadContext context) throws Exception {
        SquonkDataSource ds = context.readStreamValue(mediaType, extension, null);
        return create(ds);
    }
}
