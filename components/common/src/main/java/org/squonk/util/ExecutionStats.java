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

package org.squonk.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by timbo on 11/05/2016.
 */
public class ExecutionStats {

    private final String jobId;
    private Map<String, Integer> data = new ConcurrentHashMap<>();

    public ExecutionStats() {
        this.jobId = null;
    }

    public ExecutionStats(
            @JsonProperty("jobId") String jobId,
            @JsonProperty("data") Map<String, Integer> data) {
        this.jobId = jobId;
        this.data.putAll(data);
    }

    public ExecutionStats(String jobId, String key, int count) {
        this.jobId = jobId;
        this.data.put(key, count);
    }

    public ExecutionStats(String jobId) {
        this.jobId = jobId;
    }


    public String getJobId() {
        return jobId;
    }

    public Map<String, Integer> getData() {
        return data;
    }

    public int incrementExecutionCount(String key, int count) {
        return increment(data, key, count);
    }


    public static int increment(Map<String, Integer> data, String key, int count) {
        assert count >= 0;
        if (!data.containsKey(key)) {
            data.put(key, count);
            return count;
        } else {
            int i = data.get(key) + count;
            data.put(key, i);
            return i;
        }
    }

    @Override
    public String toString() {
        return String.format("Stats: JobID:%s metrics:[%s]", jobId,
                data.entrySet().stream().map((e) -> e.getKey().toString() + "=" + e.getValue().toString()).collect(Collectors.joining(","))
        );
    }


}