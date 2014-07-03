package toolkit.wicket.inmethod;

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

    public CheckBoxPanel(String id, IModel<T> rowModelObject) {
        super(id);
        this.rowModelObject = rowModelObject;
        addCheckBox();
    }

    private void addCheckBox() {
        CheckBox check = new CheckBox("check", new PropertyModel<Boolean>(rowModelObject, getId()));
        check.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // Do nothing (just update model)
            }
        });
        add(check);
    }
}
