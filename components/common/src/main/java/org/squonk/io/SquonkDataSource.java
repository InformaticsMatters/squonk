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

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

public abstract class SquonkDataSource implements DataSource {

    private static final Logger LOG = Logger.getLogger(SquonkDataSource.class.getName());

    public static final String ROLE_DEFAULT = "value";
    public static final String NAME_RESPONSE_BODY = "response-body";

    private String role;
    private String name;
    private final String contentType;
    private Boolean sourceGzipped;
    private boolean gzipContent = true;

    protected SquonkDataSource(String role, String name, String contentType, Boolean isSourceGzipped) {
        this(role, name, contentType);
        this.sourceGzipped = isSourceGzipped;
        if (isSourceGzipped != null) {
            gzipContent = isSourceGzipped;
        }
    }

    protected SquonkDataSource(String role, String name, String contentType) {
        this.role = role;
        this.name = name;
        this.contentType = contentType;
    }

    /** Get the role of this datasource. This mostly applies when multiple datasources are associated with a single
     * variable where the role is used to distinguish them.
     *
     * @return
     */
    public String getRole() {
        return role;
    }

    /** Get the name of this datasource. This may not always be specified, but in the case of a file it will be the name
     * of the file.
     *
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public abstract void materialize() throws IOException;

    public abstract boolean canReadMultipleTimes();

    public void setName(String name) {
        this.name = name;
    }

    /** Is the source data gzipped
     *
     * @return
     */
    public boolean isSourceGzipped() {
        return sourceGzipped;
    }

    /** Whether the InputStream returned by the {@link #getInputStream} should be gzipped
     *
     * @return
     */
    public boolean isGzipContent() {
        return gzipContent;
    }

    /** Specify whether the InputStream returned by the {@link #getInputStream} should be gzipped
     *
     * @return
     */
    public void setGzipContent(boolean gzipContent) {
        this.gzipContent = gzipContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is = getSourceInputStream();
        if (sourceGzipped != null && gzipContent == sourceGzipped) {
            return is;
        }
        if (gzipContent) {
            return IOUtils.getGzippedInputStream(is);
        } else {
            return IOUtils.getGunzippedInputStream(is);
        }
    }

    protected abstract InputStream getSourceInputStream() throws IOException;

    public FileDataSource writeTo(File dir, String baseName) throws IOException {
        InputStream is = getInputStream();
        String fname;
        if (baseName != null) {
            fname = baseName + "." + getRole();
        } else {
            fname = getName();
        }

        if (gzipContent) {
            fname = fname + ".gz";
        }
        File f = new File(dir, fname);
        LOG.warning("Writing datasource " + getName() + " to file " + f.getPath());
        Files.copy(is, f.toPath());
        FileDataSource fds = new FileDataSource(getRole(), getContentType(), f, gzipContent);
        return fds;
    }
}
