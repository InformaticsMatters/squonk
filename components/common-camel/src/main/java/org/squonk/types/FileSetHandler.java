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
import org.squonk.types.io.JsonHandler;
import org.squonk.util.IOUtils;

import java.io.*;

/**
 * Created by timbo on 23/03/2016.
 */
public class FileSetHandler implements HttpHandler<FileSet>, VariableHandler<FileSet> {


    @Override
    public Class<FileSet> getType() {
        return FileSet.class;
    }

    @Override
    public void prepareRequest(FileSet value, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (value != null) {
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(pis);
            JsonHandler.getInstance().objectToOutputStream(value, out);
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(pis) : pis);
        }
    }


    @Override
    public void writeResponse(FileSet value, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (value == null) {
            executor.setResponseBody(null);
        }  else {
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(pis);
            JsonHandler.getInstance().objectToOutputStream(value, out);
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(pis) : pis);
        }
    }

    @Override
    public FileSet readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return JsonHandler.getInstance().objectFromJson(IOUtils.getGunzippedInputStream(is), FileSet.class);
        }
        return null;
    }

    @Override
    public void writeVariable(FileSet value, WriteContext context) throws Exception {

        PipedInputStream pis = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(pis);
        JsonHandler.getInstance().objectToOutputStream(value, out);
        context.writeStreamValue(pis);
    }

    @Override
    public FileSet readVariable(ReadContext context) throws Exception {
        InputStream is = context.readStreamValue();
        return JsonHandler.getInstance().objectFromJson(is, FileSet.class);
    }
}
