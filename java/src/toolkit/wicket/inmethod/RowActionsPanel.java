package toolkit.wicket.inmethod;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;
import java.util.List;

/**
 * @author simetrias
 */
public class RowActionsPanel<T extends Serializable> extends Panel {

    private final RowActionsCallbackHandler<T> callbackHandler;
    private final T rowModelObject;

    public RowActionsPanel(String id, List<String> actionNameList, T rowModelObject, RowActionsCallbackHandler<T> callbackHandler) {
        super(id);
        this.callbackHandler = callbackHandler;
        this.rowModelObject = rowModelObject;
        addActions(actionNameList);
    }

    private void addActions(List<String> actionNameList) {
        ListView<String> actionList = new ListView<String>("actions", actionNameList) {

            @Override
            protected void populateItem(ListItem<String> components) {
                final String actionName = components.getModelObject();
                AjaxLink action = new AjaxLink("action") {

                    @Override
                    public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                        callbackHandler.onAction(ajaxRequestTarget, actionName, rowModelObject);
                    }

                    @Override
                    public boolean isVisible() {
                        return callbackHandler.isActionVisible(actionName, rowModelObject);
                    }
                };
                action.add(new AttributeModifier("class", actionName));
                components.add(action);
            }
        };
        add(actionList);
    }

}
