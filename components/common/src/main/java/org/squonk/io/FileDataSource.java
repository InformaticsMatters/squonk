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

public class FileDataSource extends SquonkDataSource {


    private final File file;

    /** The datasource name is taken from the file name
     *
     * @param role
     * @param contentType
     * @param file
     * @param gzipped
     */
    public FileDataSource(String role, String contentType, File file, Boolean gzipped) {
        super(role, file.getName(), contentType, gzipped);
        this.file = file;
    }

    /** Constructor for when you want to specify a different name for datasource.
     *
     * @param role
     * @param name
     * @param contentType
     * @param file
     * @param gzipped
     */
    public FileDataSource(String role, String name, String contentType, File file, Boolean gzipped) {
        super(role, name, contentType, gzipped);
        this.file = file;
    }

    @Override
    public InputStream getSourceInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(file);
    }

    @Override
    public void materialize() {
        // nothing needed
    }

    @Override
    public boolean canReadMultipleTimes() {
        return true;
    }

    /** Get the name of the original file as it might be different to the datasource name
     *
     * @return
     */
    public String getOriginalFilename() {
        return file.getName();
    }
}
