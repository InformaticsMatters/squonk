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

package org.squonk.types;

import org.squonk.io.SquonkDataSource;

import java.io.File;
import java.io.InputStream;

/**
 * Created by timbo on 27/03/2016.
 */
public class RDKitSDFile extends SDFile {

    public RDKitSDFile(InputStream inputStream) {
        super(inputStream, null);
    }

    public RDKitSDFile(InputStream input, Boolean gzipped) {
        super(input, gzipped);
    }

    public RDKitSDFile(File file, Boolean gzipped) {
        super(file, gzipped);
    }

    public RDKitSDFile(SquonkDataSource dataSource) {
        super(dataSource);
    }
}
