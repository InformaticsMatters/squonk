package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DeleteRowTransform extends AbstractTransform {

    private final String condition;

    protected DeleteRowTransform(@JsonProperty("condition") String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return condition;
    }
}
