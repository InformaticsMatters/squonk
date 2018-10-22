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

import org.squonk.util.CommonMimeTypes;

/**
 * Created by timbo on 23/03/2016.
 */
public class ZipFileHandler extends FileHandler<ZipFile> {

    private static final String ROLE_ZIP = "zip";

    public ZipFileHandler() {
        super(ZipFile.class, CommonMimeTypes.MIME_TYPE_ZIP_FILE, ROLE_ZIP, false);
    }

}
