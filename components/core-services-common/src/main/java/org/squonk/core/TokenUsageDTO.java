package org.squonk.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by timbo on 28/06/16.
 */
public class TokenUsageDTO {

    private final String username;
    private final String jobId;
    private final int units;
    private final String key;
    private final float tokens;
    private final Date created;

    public TokenUsageDTO(@JsonProperty("username") String username,
                         @JsonProperty("jobId") String jobId,
                         @JsonProperty("key") String key,
                         @JsonProperty("units") int units,
                         @JsonProperty("tokens") float tokens,
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

    public float getTokenCount() {
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
