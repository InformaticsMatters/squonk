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

import java.io.*;
import java.net.URL;

public class UrlDataSource extends SquonkDataSource {


    private final URL url;

    public UrlDataSource(String role, String name, String contentType, URL url, Boolean gzipped) {
        super(role, name, contentType, gzipped);
        this.url = url;
    }

    @Override
    public InputStream getSourceInputStream() throws IOException {
        return url.openStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Writing to URL not supported");
    }

    @Override
    public void materialize() {
        // nothing needed
    }

    @Override
    public boolean canReadMultipleTimes() {
        return true;
    }
}
