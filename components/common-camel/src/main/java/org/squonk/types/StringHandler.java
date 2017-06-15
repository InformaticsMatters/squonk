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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by timbo on 23/03/2016.
 */
public class StringHandler implements HttpHandler<String>, VariableHandler<String> {

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public void prepareRequest(String s, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (s != null) {
            InputStream is = new ByteArrayInputStream(s.getBytes());
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public void writeResponse(String s, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (s == null) {
            executor.setResponseBody(null);
        }  else {
            InputStream is = new ByteArrayInputStream(s.getBytes());
            executor.setResponseBody(gzip ? IOUtils.getGzippedInputStream(is) : is);
        }
    }

    @Override
    public String readResponse(RequestResponseExecutor executor, boolean gunzip) throws IOException {
        InputStream is = executor.getResponseBody();
        if (is != null) {
            return IOUtils.convertStreamToString(gunzip ? IOUtils.getGunzippedInputStream(is) : is);
        }
        return null;
    }

    @Override
    public void writeVariable(String value, WriteContext context) throws Exception {
        context.writeTextValue(value);
    }

    @Override
    public String readVariable(ReadContext context) throws Exception {
        return context.readTextValue();
    }
}
