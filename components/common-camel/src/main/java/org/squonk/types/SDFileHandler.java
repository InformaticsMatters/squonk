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
import java.util.logging.Logger;

/**
 * Created by timbo on 23/03/2016.
 */
public class SDFileHandler implements HttpHandler<SDFile>, VariableHandler<SDFile> {

    private static final Logger LOG = Logger.getLogger(SDFileHandler.class.getName());
    private static final String KEY = "sdf.gz";

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
    public void writeVariable(SDFile sdf, WriteContext context) throws Exception {
        LOG.info("Writing as SDFile");
        //context.writeStreamValue(sdf.getInputStream());
        context.writeSingleStreamValue(sdf.getInputStream(), KEY, true);
    }

    @Override
    public SDFile readVariable(ReadContext context) throws Exception {
        //InputStream is = context.readStreamValue();
        InputStream is = context.readSingleStreamValue(KEY, true);
        return new SDFile(is);
    }
}
