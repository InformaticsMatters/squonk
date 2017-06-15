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

package org.squonk.api;

import org.squonk.io.IODescriptor;
import org.squonk.util.CommonMimeTypes;

/**
 * Created by timbo on 20/03/2016.
 */
public interface MimeTypeResolver extends CommonMimeTypes {

    Class resolvePrimaryType(String mimeType);

    Class resolveGenericType(String mimeType);

    IODescriptor createIODescriptor(String name, String mediaType);

    default HttpHandler createHttpHandler(String mimeType) {
        return createHttpHandler(resolvePrimaryType(mimeType), resolveGenericType(mimeType));
    }

    default HttpHandler createHttpHandler(Class primaryType) {
        return createHttpHandler(primaryType, null);
    }

    HttpHandler createHttpHandler(Class primaryType, Class genericType);

    default VariableHandler createVariableHandler(String mimeType) {
        return createVariableHandler(resolvePrimaryType(mimeType), resolveGenericType(mimeType));
    }

    default VariableHandler createVariableHandler(Class primaryType) {
        return createVariableHandler(primaryType, null);
    }

    VariableHandler createVariableHandler(Class primaryType, Class secondaryType);
}



