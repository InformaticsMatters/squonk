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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StringDataSource extends SquonkDataSource {

    private final String string;

    public StringDataSource(String role, String name, String contentType, String string, Boolean gzipped) {
        super(role, name, contentType, gzipped);
        this.string = string;
    }

    @Override
    public InputStream getSourceInputStream() throws IOException {
        return new ByteArrayInputStream(string.getBytes());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Writing to String not supported");
    }

    @Override
    public void materialize() throws IOException {
        // nothing needed
    }

    @Override
    public boolean canReadMultipleTimes() {
        return true;
    }
}
