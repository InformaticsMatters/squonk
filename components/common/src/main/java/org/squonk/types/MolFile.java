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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/** Wrapper around data from a MDL molfile to allow strong typing and type conversion
 *  Not to be confused with Mol2File which is for Tripos Mol2 format.
 *
 * @author timbo
 */
public class MolFile extends AbstractStreamType {

    private static final String MEDIA_TYPE = CommonMimeTypes.MIME_TYPE_MDL_MOLFILE;

    public MolFile(InputStream input) {
        super(input);
    }

    public MolFile(String input) {
        super(new ByteArrayInputStream(input.getBytes()));
    }

    public String getMediaType() {
        return MEDIA_TYPE;
    }
    
}
