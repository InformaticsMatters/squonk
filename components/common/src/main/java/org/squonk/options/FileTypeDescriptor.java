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

package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.options.types.Structure;

import java.io.File;

/** Type descriptor for a File that has to be uploaded
 * Created by timbo on 03/02/16.
 */
public class FileTypeDescriptor extends SimpleTypeDescriptor<File> {

    private final String[] fileTypes;

    public FileTypeDescriptor(@JsonProperty("fileTypes") String[] fileTypes) {
        super(File.class);
        this.fileTypes = fileTypes;
    }

    public  String[] getFileTypes() {
        return fileTypes;
    }


}
