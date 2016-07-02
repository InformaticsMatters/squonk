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
