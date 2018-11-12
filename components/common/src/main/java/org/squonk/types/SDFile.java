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

package org.squonk.types;

import org.squonk.io.SquonkDataSource;
import org.squonk.util.CommonMimeTypes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/** Wrapper around data from an SD file to allow strong typing and type conversion
 *
 * @author timbo
 */
public class SDFile extends AbstractStreamType {

    public static final String PROP_NAME_FIELD_NAME = "SDF_NAME_FIELD_NAME";
    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_MDL_SDF;
    
    public SDFile(InputStream input, Boolean gzipped) {
        super(input, MEDIA_TYPE, gzipped);
    }

    public SDFile(String data, Boolean gzipped) {
        this(new ByteArrayInputStream(data.getBytes()), gzipped);
    }

    public SDFile(File file, Boolean gzipped) {
        super(file, MEDIA_TYPE, gzipped);
    }

    public SDFile(SquonkDataSource dataSource) {
        super(dataSource, MEDIA_TYPE);
    }
}
