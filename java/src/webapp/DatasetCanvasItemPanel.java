package webapp;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class DatasetCanvasItemPanel extends Panel {

    private static final String POSITION_X_PARAM_NAME = "positionX";
    private static final String POSITION_Y_PARAM_NAME = "positionY";
    private final DatasetCanvasItemModel model;

    @Inject
    private SessionData sessionData;

    public DatasetCanvasItemPanel(String id, DatasetCanvasItemModel model) {
        super(id);
        this.model = model;
        setOutputMarkupId(true);
        add(new Label("id", model.getId()));
        addCanvasDragStopBehavior();
    }

    private void addCanvasDragStopBehavior() {
        AbstractDefaultAjaxBehavior onCanvasDragStopBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                String x = getRequest().getRequestParameters().getParameterValue(POSITION_X_PARAM_NAME).toString();
                String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y_PARAM_NAME).toString();
                // System.out.println("Dragged to: " + POSITION_X_PARAM_NAME + ": " + x + " " + POSITION_Y_PARAM_NAME + ": " + y);

                System.out.println(sessionData.getCanvasItemList().indexOf(DatasetCanvasItemPanel.this.model));

                DatasetCanvasItemPanel.this.model.setPositionX(x);
                DatasetCanvasItemPanel.this.model.setPositionY(y);

                target.add(DatasetCanvasItemPanel.this);
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                String callBackScript = getCallbackFunction(CallbackParameter.explicit(POSITION_X_PARAM_NAME), CallbackParameter.explicit(POSITION_Y_PARAM_NAME)).toString();
                callBackScript = "onCanvasDragStop=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasDragStopBehavior);
    }
}
