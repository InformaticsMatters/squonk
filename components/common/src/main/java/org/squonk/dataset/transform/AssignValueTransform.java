package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AssignValueTransform extends AbstractTransform {

    private final String fieldName;
    private final String expression;
    private final String condition;
    private final String onError;


    protected AssignValueTransform(@JsonProperty("fieldName") String fieldName,
                                   @JsonProperty("expression") String expression,
                                   @JsonProperty("condition") String condition,
                                   @JsonProperty("onError") String onError) {
        this.fieldName = fieldName;
        this.expression = expression;
        this.condition = condition;
        this.onError = onError;
    }

    AssignValueTransform(String fieldName, String expression, String condition) {
        this(fieldName, expression, condition, null);
    }

    AssignValueTransform(String fieldName, String expression) {
        this(fieldName, expression, null, null);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getExpression() {
        return expression;
    }

    public String getCondition() {
        return condition;
    }

    public String getOnError() {
        return onError;
    }

}
