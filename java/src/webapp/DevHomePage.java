package webapp;

import localservice.DevConfig;
import localservice.DevLocalService;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import toolkit.wicket.layout.LayoutResourceReference;
import toolkit.wicket.style.simple.SimpleStyleResourceReference;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author simetrias
 */
public class DevHomePage extends WebPage {

    private static final String POSITION_X_PARAM_NAME = "positionX";
    private static final String POSITION_Y_PARAM_NAME = "positionY";

    @Inject
    private DevConfig config;
    @Inject
    private DevLocalService localService;

    private WebMarkupContainer plumbContainer;
    private WebMarkupContainer source;
    private WebMarkupContainer target;
    private AbstractDefaultAjaxBehavior onCanvasDropBehavior;
    private ArrayList<DatasetCanvasItemModel> canvasItemList;
    private RepeatingView repeatingView;

    public DevHomePage() {
        addCanvas();
        addDatasetsPanel();
        addCanvasDropBehavior();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(LayoutResourceReference.get()));
        response.render(CssHeaderItem.forReference(SimpleStyleResourceReference.get()));
        response.render(CssHeaderItem.forReference(new CssResourceReference(DevHomePage.class, "resources/flow.css")));
        response.render(CssHeaderItem.forReference(new CssResourceReference(DevHomePage.class, "resources/lac.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(DevHomePage.class, "resources/js/dom.jsPlumb-1.6.2.js")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(DevHomePage.class, "resources/MainPage.js")));
        response.render(OnDomReadyHeaderItem.forScript("init();"));
    }

    private void addCanvas() {
        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        add(plumbContainer);

        canvasItemList = new ArrayList<DatasetCanvasItemModel>();
        ListView<DatasetCanvasItemModel> listView = new ListView<DatasetCanvasItemModel>("canvasItem", canvasItemList) {

            @Override
            protected void populateItem(ListItem<DatasetCanvasItemModel> components) {
                DatasetCanvasItemModel model = components.getModelObject();
                components.add(new AttributeModifier("style", "top:" + model.getInitialY() + "px; left:" + model.getInitialX() + "px;"));
                components.add(new DatasetCanvasItemPanel("item", model));
            }
        };
        plumbContainer.add(listView);
    }

    private void addCanvasItem(AjaxRequestTarget target) {
        String x = getRequest().getRequestParameters().getParameterValue(POSITION_X_PARAM_NAME).toString();
        String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y_PARAM_NAME).toString();
        System.out.println(POSITION_X_PARAM_NAME + ": " + x + " " + POSITION_Y_PARAM_NAME + ": " + y);

        DatasetCanvasItemModel newItemModel = new DatasetCanvasItemModel();
        newItemModel.setInitialX(x);
        newItemModel.setInitialY(y);
        canvasItemList.add(newItemModel);

        target.add(plumbContainer);
        target.appendJavaScript("setupCanvas(); makeCanvasItemsDraggable()"); // since we are replacing the plumb container entirely

    }

    private void addCanvasDropBehavior() {
        onCanvasDropBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                addCanvasItem(target);
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                String callBackScript = getCallbackFunction(CallbackParameter.explicit(POSITION_X_PARAM_NAME), CallbackParameter.explicit(POSITION_Y_PARAM_NAME)).toString();
                callBackScript = "onCanvasDrop=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasDropBehavior);
    }

    private void addDatasetsPanel() {
        add(new DatasetsPanel("datasets"));
    }
}
