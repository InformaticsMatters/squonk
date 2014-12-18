package com.im.lac.wicket.inmethod;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class CheckBoxPanel<T extends Serializable> extends Panel {

    private final IModel<T> rowModelObject;
    private final CheckBoxColumnCallbackHandler<T> checkBoxColumnCallbackHandler;

    protected CheckBoxPanel(String id, IModel<T> rowModelObject, CheckBoxColumnCallbackHandler<T> checkBoxColumnCallbackHandler) {
        super(id);
        this.checkBoxColumnCallbackHandler = checkBoxColumnCallbackHandler;
        this.rowModelObject = rowModelObject;
        addCheckBox();
    }

    private void addCheckBox() {
        Boolean isEnabled = checkBoxColumnCallbackHandler.isEnabled(rowModelObject.getObject());
        CheckBox check = new CheckBox("check", new PropertyModel<Boolean>(rowModelObject, getId()));
        check.setEnabled(isEnabled);
        if (!isEnabled) {
            String disabledTitle = checkBoxColumnCallbackHandler.getDisabledTitle(rowModelObject.getObject());
            if (disabledTitle != null) {
                check.add(new AttributeModifier("title", disabledTitle));
            }
        }
        check.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // Do nothing (just update model)
            }
        });
        add(check);
    }
}
