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
import org.squonk.client.VariableClient;
import org.squonk.util.IOUtils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Created by timbo on 13/03/16.
 */
public class FilesystemReadContext extends AbstractFilesystemContext implements VariableHandler.ReadContext {

    private static final Logger LOG = Logger.getLogger(FilesystemReadContext.class.getName());

    public FilesystemReadContext(File dir, String baseName) {
        super(dir, baseName);
    }

    @Override
    public String readTextValue(String key) throws IOException {
        File f = generateFile(key);
        if (f.exists()) {
            StringBuilder b = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                reader.lines().forEach(l -> b.append(l));
            }
            return b.toString();
        } else {
            return null;
        }
    }

    @Override
    public String readSingleTextValue(String key) throws Exception {
        return readTextValue(key);
    }

    @Override
    public InputStream readStreamValue(String key, boolean gzip) throws Exception {
        File f = generateFile(key);
        LOG.log(Level.INFO, "Reading stream value. key: {0} gzip: {1}", new Object[] {key, gzip});
        if (f.exists()) {
            return gzip ? new GZIPInputStream(new FileInputStream(f)) : new FileInputStream(f);
        } else {
            return null;
        }
    }

}
