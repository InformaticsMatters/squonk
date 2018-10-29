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
import org.squonk.io.SquonkDataSource;
import org.squonk.util.CommonMimeTypes;
import org.squonk.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by timbo on 23/03/2016.
 */
public class SDFileHandler extends DefaultHandler<SDFile> {

    private static final Logger LOG = Logger.getLogger(SDFileHandler.class.getName());
    private static final String ROLE_SDF = "sdf";


    public SDFileHandler() {
        super(SDFile.class);
    }

    @Override
    public void prepareRequest(SDFile sdf, RequestResponseExecutor executor, boolean gzipRequest, boolean gzipResponse) throws IOException {
        if (sdf == null) {
            throw new NullPointerException("SDF cannot be null");
        }
        handleGzipHeaders(executor, gzipRequest, gzipResponse);
        executor.prepareRequestBody(gzipRequest ? IOUtils.getGzippedInputStream(sdf.getInputStream()) : sdf.getInputStream());
    }

    @Override
    public void writeResponse(SDFile sdf, RequestResponseExecutor executor, boolean gzip) throws IOException {
        LOG.fine("Writing SDF response. Gzip? " + gzip);
        if (sdf == null) {
            executor.setResponseBody(null);
        } else {
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(sdf.getInputStream()) : sdf.getInputStream());
            if (gzip) {
                executor.setResponseHeader("Content-Encoding", "gzip");
            }
        }
    }

    @Override
    public SDFile readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return new SDFile(gunzip ? IOUtils.getGunzippedInputStream(is) : is, !gunzip);
        }
        return null;
    }

    @Override
    public void writeVariable(SDFile sdf, WriteContext context) throws Exception {
        LOG.fine("Writing as SDFile");
        //context.writeStreamValue(sdf.getInputStream());
        context.writeStreamValue(sdf.getInputStream(), CommonMimeTypes.MIME_TYPE_MDL_SDF, ROLE_SDF,null, true);
    }

    @Override
    public SDFile readVariable(ReadContext context) throws Exception {
        SquonkDataSource ds = context.readStreamValue(CommonMimeTypes.MIME_TYPE_MDL_SDF, ROLE_SDF);
        return new SDFile(ds);
    }

    @Override
    public List<SquonkDataSource> readDataSources(ReadContext context) throws Exception {
        SquonkDataSource ds = context.readStreamValue(CommonMimeTypes.MIME_TYPE_MDL_SDF, ROLE_SDF);
        return Collections.singletonList(ds);
    }

}
