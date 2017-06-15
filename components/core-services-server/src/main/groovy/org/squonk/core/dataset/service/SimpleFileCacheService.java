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

package org.squonk.core.dataset.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author timbo
 */
public class SimpleFileCacheService {

    private static final Logger LOG = Logger.getLogger(SimpleFileCacheService.class.getName());

    private final File cacheDir;

    public SimpleFileCacheService(String cacheDir) {
        this.cacheDir = new File(cacheDir);
        LOG.log(Level.INFO, "FileCache using dir of {0}", cacheDir);
    }

    /**
     * Deletes and creates the cache dir to ensure its empty
     */
    public void init() throws IOException {
        if (cacheDir.exists()) {
            deleteRecursive(cacheDir);
        }
        Files.createDirectories(cacheDir.toPath());
    }

    boolean deleteRecursive(File path) throws FileNotFoundException {
        boolean ret = deleteContentsRecursive(cacheDir);
        return ret && path.delete();
    }

    boolean deleteContentsRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) {
            throw new FileNotFoundException(path.getAbsolutePath());
        }
        boolean ret = true;
        if (path.isDirectory()) {
            for (File f : path.listFiles()) {
                ret = ret && deleteContentsRecursive(f);
                ret = ret && f.delete();
            }
        }
        return ret;
    }

    public void clearCache() throws FileNotFoundException {
        deleteContentsRecursive(cacheDir);
    }

    public File getFileFromCache(Long id) {
        LOG.log(Level.INFO, "Getting file {0} from cache", id);
        File f = new File(cacheDir, "" + id);
        if (f.exists()) {
            return f;
        } else {
            return null;
        }
    }

    public boolean deleteFileFromCache(Long id) {
        LOG.log(Level.INFO, "Deleting file {0} from cache", id);
        File f = new File(cacheDir, "" + id);
        if (f.exists()) {
            return f.delete();
        } else {
            return false;
        }
    }

    public File addFileToCache(Long id, InputStream is) throws IOException {
        LOG.log(Level.INFO, "Adding file {0} to cache", id);
        File f = new File(cacheDir, "" + id);
        if (f.exists()) {
            f.delete();
        }
        Files.copy(is, f.toPath());
        return f;
    }

}
