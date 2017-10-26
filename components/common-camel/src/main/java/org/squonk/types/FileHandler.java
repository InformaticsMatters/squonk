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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/** Generic handler for files that handle their data as an InputStream. Should cover files of any format.
 * To use this class create a concrete subclass and just call the constructor.
 *
 * Created by timbo on 23/03/2016.
 */
abstract class FileHandler<T extends AbstractStreamType> implements HttpHandler<T>, VariableHandler<T> {

    private static final Logger LOG = Logger.getLogger(FileHandler.class.getName());

    private final Class<T> type;
    private final String extension;

    FileHandler(Class<T> type, String extension) {
        this.type = type;
        this.extension = extension;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public void prepareRequest(T file, RequestResponseExecutor executor, boolean gzip) throws IOException {
        if (file != null) {
            executor.prepareRequestBody(gzip ? IOUtils.getGzippedInputStream(file.getInputStream()) : file.getInputStream());
        }
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
            return create(gunzip ? IOUtils.getGunzippedInputStream(is) : is);
        }
        return null;
    }

    @Override
    public void writeVariable(T file, WriteContext context) throws Exception {
        boolean gzip = shouldGzip(extension);
        LOG.info("Writing variable: gzip? " + gzip);
        context.writeSingleStreamValue(file.getInputStream(), extension, gzip);
    }

    @Override
    public T readVariable(ReadContext context) throws Exception {
        boolean gzip = shouldGzip(extension);
        LOG.info("Reading variable: gzip? " + gzip);
        InputStream is = context.readSingleStreamValue(extension, gzip);
        return create(is);
    }

    private T create(InputStream is) {
        try {
            Constructor c = type.getConstructor(InputStream.class);
            return (T)c.newInstance(is);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            // close the InputStream otherwise it can result in connection leaks.
            IOUtils.close(is);
            throw new RuntimeException(type.getName() + " is not a valid AbstractStreamType as it does not seem to have a constructor for an InputStream", e);
        }

    }
}
