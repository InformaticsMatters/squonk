package toolkit.wicket.inmethod;

import org.apache.wicket.ajax.AjaxRequestTarget;

import java.io.Serializable;

/**
 * @author simetrias
 */
public abstract class RowActionsCallbackHandler<T> implements Serializable {

    public abstract void onAction(AjaxRequestTarget target, String name, T rowModelObject);

    public boolean isActionVisible(String name, T rowModelObject) {
        return true;
    }
}
