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

package org.squonk.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.squonk.util.Utils;

import java.io.Serializable;

/**
 * Created by timbo on 07/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IODescriptor<P,Q> implements Serializable {

    private final String name;
    private final String mediaType;
    private final Class<P> primaryType;
    private final Class<Q> secondaryType;

    public IODescriptor(
            @JsonProperty("name") String name,
            @JsonProperty("mediaType") String mediaType,
            @JsonProperty("primaryType") Class<P> primaryType,
            @JsonProperty("secondaryType") Class<Q> secondaryType
        ) {
        this.name = name;
        this.mediaType = mediaType;
        this.primaryType = primaryType;
        this.secondaryType = secondaryType;
    }

    public IODescriptor(String name, String mediaType, Class<P> primaryType) {
        this(name, mediaType, primaryType, null);
    }

    public String getName() {
        return name;
    }

    public String getMediaType() {
        return mediaType;
    }

    public Class<P> getPrimaryType() {
        return primaryType;
    }

    public Class<Q> getSecondaryType() {
        return secondaryType;
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("IODescriptor[")
                .append("name=").append(name)
                .append(" mediaType=").append(mediaType)
                .append(" primaryType=").append(primaryType)
                .append(" secondaryType=").append(secondaryType)
                .append("]");
        return b.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof IODescriptor) {
            IODescriptor other = (IODescriptor)o;
            return Utils.safeEqualsIncludeNull(name, other.getName())
                    && Utils.safeEqualsIncludeNull(mediaType, other.getMediaType())
                    && Utils.safeEqualsIncludeNull(primaryType, other.getPrimaryType())
                    && Utils.safeEqualsIncludeNull(secondaryType, other.getSecondaryType())
                    ;
        }
        return false;
    }
}
