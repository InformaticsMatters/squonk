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

package org.squonk.execution.variable.impl;


import org.squonk.api.VariableHandler;
import org.squonk.util.IOUtils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by timbo on 13/03/16.
 */
public class FilesystemWriteContext extends AbstractFilesystemContext implements VariableHandler.WriteContext {


    private static final Logger LOG = Logger.getLogger(FilesystemWriteContext.class.getName());

    public FilesystemWriteContext(File dir, String baseName) {
        super(dir, baseName);
        LOG.fine("Dir: " + dir + " BaseName: " + baseName);
    }

    @Override
    public void writeTextValue(String value, String mediaType, String role, String key) throws IOException {
        File f = generateFile(role, false);
        LOG.fine("Writing text to file " + f.getPath());
        try (FileWriter out = new FileWriter(f)) {
            out.append(value);
        }
    }

    @Override
    public void writeStreamValue(InputStream value, String mediaType, String role, String key, boolean gzip) throws Exception {
        File f = generateFile(role, gzip);
        LOG.log(Level.FINE, "Writing stream value. file: {0} gzip: {1}", new Object[] {f.getPath(), gzip});
        try (OutputStream out = new FileOutputStream(f)) {
            IOUtils.transfer(gzip ? IOUtils.getGzippedInputStream(value) : IOUtils.getGunzippedInputStream(value), out, 4096);
        }
    }

    @Override
    public void deleteVariable() throws Exception {
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith(baseName)) {
                f.delete();
            }
        }
    }

}
