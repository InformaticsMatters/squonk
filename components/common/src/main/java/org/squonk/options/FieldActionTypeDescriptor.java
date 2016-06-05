package org.squonk.options;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** Defines a set of action mappings for a set of input fields.
 * Currently actions are simple (e.g. add, delete) but this will need to support additional parameters e.g.
 * for an "rename" action we need to be able able to define that when this action is selected an additional element should
 * be present that allows to specify the new name
 *
 * Created by timbo on 03/02/16.
 */
public class FieldActionTypeDescriptor extends SimpleTypeDescriptor<FieldActionMapping> {

    private final String[] actions;

    public FieldActionTypeDescriptor(@JsonProperty("actions") String[] actions) {
        super(FieldActionMapping.class);
        this.actions = actions;
    }

    public String[] getActions() {
        return actions;
    }

}
