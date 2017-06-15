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

import org.apache.camel.spi.TypeConverterRegistry;

/**
 * Created by timbo on 23/03/2016.
 */
public interface GenericHandler<P,G> {

    void setGenericType(Class<G> genericType);

    Class<G> getGenericType();

     default boolean canConvertGeneric(Class<? extends Object> otherGenericType, TypeConverterRegistry registry) {
         return false;
     }

    default P convertGeneric(P from, Class<? extends Object> otherGenericType, TypeConverterRegistry registry) {
        throw new RuntimeException("There is no default way to handle generic conversions. Implementations must handle this.");
    }
}
