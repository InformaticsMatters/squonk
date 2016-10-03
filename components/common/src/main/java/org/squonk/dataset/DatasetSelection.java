package org.squonk.dataset;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.*;

/**
 * Created by timbo on 03/10/16.
 */
public class DatasetSelection implements Serializable {

    private final Set<UUID> uuids = new HashSet<>();

    public DatasetSelection(@JsonProperty("uuids") Collection<UUID> uuids) {
        if (uuids != null) {
            this.uuids.addAll(uuids);
        }
    }

    public Set<UUID> getUuids() {
        return uuids;
    }
}
