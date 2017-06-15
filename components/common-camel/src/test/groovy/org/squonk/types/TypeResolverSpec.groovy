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

package org.squonk.types

import org.squonk.api.HttpHandler
import org.squonk.api.MimeTypeResolver
import org.squonk.api.VariableHandler
import org.squonk.dataset.Dataset
import spock.lang.Specification

/**
 * Created by timbo on 23/03/2016.
 */
class TypeResolverSpec extends Specification {

    void "resolve types"()  {
        TypeResolver resolver = new TypeResolver()

        when:
        def primary = resolver.resolvePrimaryType(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)
        def generic  = resolver.resolveGenericType(MimeTypeResolver.MIME_TYPE_DATASET_MOLECULE_JSON)

        then:
        primary == Dataset.class
        generic == MoleculeObject.class
    }

    void "resolve http handler"()  {
        TypeResolver resolver = new TypeResolver()

        when:
        def h = resolver.createHttpHandler(Dataset.class)

        then:
        h != null
        h.class == DatasetHandler.class
        h instanceof HttpHandler
    }

    void "resolve variable handler"()  {
        TypeResolver resolver = new TypeResolver()

        when:
        def h = resolver.createVariableHandler(Dataset.class)

        then:
        h != null
        h.class == DatasetHandler.class
        h instanceof VariableHandler
    }

}
