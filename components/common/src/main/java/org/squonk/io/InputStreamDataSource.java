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

package org.squonk.io;

import org.squonk.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamDataSource extends SquonkDataSource {

    private final InputStream inputStream;
    private byte[] materialised = null;

    public InputStreamDataSource(String name, String contentType, InputStream inputStream, Boolean gzipped) {
        super(name, contentType, gzipped);
        this.inputStream = inputStream;
    }

    @Override
    public InputStream getSourceInputStream() throws IOException {
        if (materialised != null) {
            return new ByteArrayInputStream(materialised);
        } else {
            return inputStream;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Writing to InputStream not supported");
    }

    @Override
    public void materialize() throws IOException {
        if (inputStream == null) {
            // strange!
        } else if (inputStream instanceof ByteArrayInputStream) {
            // nothing needed
        } else {
            materialised = IOUtils.convertStreamToBytes(inputStream);
        }
    }

    @Override
    public boolean canReadMultipleTimes() {
        return materialised != null;
    }
}
