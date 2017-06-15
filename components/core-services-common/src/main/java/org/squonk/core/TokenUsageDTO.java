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
public class TokenUsageDTO {

    private final String username;
    private final String jobId;
    private final Integer units;
    private final String key;
    private final Float tokens;
    private final Date created;

    public TokenUsageDTO(@JsonProperty("username") String username,
                         @JsonProperty("jobId") String jobId,
                         @JsonProperty("key") String key,
                         @JsonProperty("units") Integer units,
                         @JsonProperty("tokens") Float tokens,
                         @JsonProperty("created") Date created) {
        this.username = username;
        this.jobId = jobId;
        this.units = units;
        this.key = key;
        this.tokens = tokens;
        this.created = created;
    }


    public String getUsername() {
        return username;
    }

    public String getJobId() {
        return jobId;
    }

    public String getKey() {
        return key;
    }

    public Integer getUnits() {
        return units;
    }

    public Float getTokenCount() {
        return tokens;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return String.format("TokenUsageDTO: username=%s jobId=%s key=%s units=%s tokens=%s", username, jobId, key, units, tokens);
    }
}
