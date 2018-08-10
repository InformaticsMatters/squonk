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

package org.squonk.execution.variable.impl;

import org.squonk.api.VariableHandler;
import org.squonk.io.FileDataSource;
import org.squonk.io.SquonkDataSource;

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by timbo on 13/03/16.
 */
public class FilesystemReadContext extends AbstractFilesystemContext implements VariableHandler.ReadContext {

    private static final Logger LOG = Logger.getLogger(FilesystemReadContext.class.getName());

    /**
     *
     * @param dir The directory containing the file
     * @param baseName The base name of the file
     */
    public FilesystemReadContext(File dir, String baseName) {
        super(dir, baseName);
    }

    @Override
    public String readTextValue(String mediaType, String extension, String key) throws IOException {
        File f = generateFile(extension, false);
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
    public SquonkDataSource readStreamValue(String mediaType, String extension, String key) throws Exception {
        File f = findFile(extension);
        if (f != null) {
            LOG.warning("Reading file " + f.getName());
            boolean gzipped = f.getName().toLowerCase().endsWith(".gz");
            return new FileDataSource(f.getName(), mediaType, f, gzipped);
        } else {
            return null;
        }
    }
}
