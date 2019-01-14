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

package org.squonk.chemaxon.enumeration;

import org.squonk.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by timbo on 05/07/16.
 */
public class ReactionLibrary {

    private static final String PREFIX = "chemaxon_reaction_library/";
    private static final String EXTENSION = ".mrv";

    private final ZipFile zipFile;
    private final String path;

    public ReactionLibrary(String path) throws IOException {
        this.path = path;
        this.zipFile = new ZipFile(path);
    }

    public ReactionLibrary(File file) throws IOException {
        this.path = file.getPath();
        this.zipFile = new ZipFile(file);
    }

    public String getPath() {
        return path;
    }

    public List<String> getReactionNames() {
        Stream<? extends ZipEntry> entries = zipFile.stream();
        List<String> names = entries.map((e) -> e.getName())
                .filter((s) -> s.endsWith(EXTENSION) && s.startsWith(PREFIX))
                .map((s) -> s.substring(26, s.length() - 4))
                .collect(Collectors.toList());
        return names;
    }

    public String getReaction(String name) throws IOException {
        ZipEntry entry = zipFile.getEntry(PREFIX + name + EXTENSION);
        return entry == null ? null : IOUtils.convertStreamToString(zipFile.getInputStream(entry));
    }
}
