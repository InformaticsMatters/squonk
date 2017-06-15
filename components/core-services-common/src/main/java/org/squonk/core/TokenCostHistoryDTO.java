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

package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by timbo on 28/06/16.
 */
public class TokenCostHistoryDTO {

    private final Integer id;
    private final String key;
    private final float cost;
    private final Date created;

    public TokenCostHistoryDTO(@JsonProperty("id") Integer id, @JsonProperty("key") String key, @JsonProperty("cost") float cost, @JsonProperty("created") Date created) {
        this.id = id;
        this.key = key;
        this.cost = cost;
        this.created = created;
    }


    public Integer getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public float getCost() {
        return cost;
    }

    public Date getCreated() {
        return created;
    }
}
