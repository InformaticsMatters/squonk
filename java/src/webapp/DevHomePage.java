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
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import toolkit.wicket.layout.LayoutResourceReference;
import toolkit.wicket.style.simple.SimpleStyleResourceReference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author simetrias
 */
public class DevHomePage extends WebPage {

    private static final String POSITION_X_PARAM_NAME = "positionX";
    private static final String POSITION_Y_PARAM_NAME = "positionY";
    private static final String CANVASITEM_INDEX_PARAM_NAME = "index";
    private static final String CANVASITEM_REPEATER_WICKETID = "canvasItem";

    private List<AbstractCanvasItemModel> canvasItemModelList = new ArrayList<AbstractCanvasItemModel>();

    @Inject
    private DevConfig config;
    @Inject
    private DevLocalService localService;

    private WebMarkupContainer plumbContainer;
    private ListView<AbstractCanvasItemModel> canvasItemRepeater;

    public DevHomePage() {
        addCanvas();
        addDatasetsPanel();
        addCanvasDropBehavior();
        addCanvasItemDragStopBehavior();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(LayoutResourceReference.get()));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(DevHomePage.class, "resources/jquery.layout.resizePaneAccordions-1.2.min.js")));
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

        canvasItemRepeater = new ListView<AbstractCanvasItemModel>(CANVASITEM_REPEATER_WICKETID, canvasItemModelList) {

            @Override
            protected void populateItem(ListItem<AbstractCanvasItemModel> components) {
                components.setOutputMarkupId(true);
                AbstractCanvasItemModel model = components.getModelObject();
                components.add(new AttributeModifier("style", "top:" + model.getPositionY() + "px; left:" + model.getPositionX() + "px;"));
                if (model instanceof DatasetCanvasItemModel) {
                    DatasetCanvasItemModel datasetCanvasItemModel = (DatasetCanvasItemModel) model;
                    components.add(new DatasetCanvasItemPanel("item", datasetCanvasItemModel));
                }
            }
        };
        plumbContainer.add(canvasItemRepeater);
    }

    private void addCanvasItem(AjaxRequestTarget target) {
        String x = getRequest().getRequestParameters().getParameterValue(POSITION_X_PARAM_NAME).toString();
        String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y_PARAM_NAME).toString();
        System.out.println("Added at: " + POSITION_X_PARAM_NAME + ": " + x + " " + POSITION_Y_PARAM_NAME + ": " + y);

        int index = canvasItemModelList.size();

        DatasetCanvasItemModel newItemModel = new DatasetCanvasItemModel();
        newItemModel.setId(Integer.toString(index));
        newItemModel.setPositionX(x);
        newItemModel.setPositionY(y);
        canvasItemModelList.add(newItemModel);

        ListItem listItem = new ListItem("canvasItem" + index, index);
        listItem.setOutputMarkupId(true);
        listItem.add(new AttributeModifier("style", "top:" + newItemModel.getPositionY() + "px; left:" + newItemModel.getPositionX() + "px;"));
        canvasItemRepeater.add(listItem);

        DatasetCanvasItemPanel datasetCanvasItemPanel = new DatasetCanvasItemPanel("item", newItemModel);
        listItem.add(datasetCanvasItemPanel);

        String script = "$('#:plumbContainer').append(\"<div class=':class' id=':itemId'></div>\")";
        target.prependJavaScript(script
                .replaceAll(":plumbContainer", plumbContainer.getMarkupId())
                .replaceAll(":class", "canvas-item")
                .replaceAll(":itemId", listItem.getMarkupId()));
        target.add(listItem);

        target.appendJavaScript("makeCanvasItemsDraggable(':itemId')".replaceAll(":itemId", "#" + listItem.getMarkupId()));
        target.appendJavaScript("addSourceEndpoint(':itemId')".replaceAll(":itemId", listItem.getMarkupId()));
        target.appendJavaScript("addTargetEndpoint(':itemId')".replaceAll(":itemId", listItem.getMarkupId()));
    }

    private void addCanvasDropBehavior() {
        AbstractDefaultAjaxBehavior onCanvasDropBehavior = new AbstractDefaultAjaxBehavior() {

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

    private void addCanvasItemDragStopBehavior() {
        AbstractDefaultAjaxBehavior onCanvasItemDragStopBehavior = new AbstractDefaultAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                String index = getRequest().getRequestParameters().getParameterValue(CANVASITEM_INDEX_PARAM_NAME).toString();
                String x = getRequest().getRequestParameters().getParameterValue(POSITION_X_PARAM_NAME).toString();
                String y = getRequest().getRequestParameters().getParameterValue(POSITION_Y_PARAM_NAME).toString();
                System.out.println("Item index " + index + " Dragged to: " + POSITION_X_PARAM_NAME + ": " + x + " " + POSITION_Y_PARAM_NAME + ": " + y);

                int i = Integer.parseInt(index);
                AbstractCanvasItemModel model = canvasItemModelList.get(i);
                model.setPositionX(x);
                model.setPositionY(y);
            }

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                String callBackScript = getCallbackFunction(
                        CallbackParameter.explicit(CANVASITEM_INDEX_PARAM_NAME),
                        CallbackParameter.explicit(POSITION_X_PARAM_NAME),
                        CallbackParameter.explicit(POSITION_Y_PARAM_NAME)).toString();
                callBackScript = "onCanvasItemDragStop=" + callBackScript + ";";
                response.render(OnDomReadyHeaderItem.forScript(callBackScript));
            }
        };
        add(onCanvasItemDragStopBehavior);
    }

    private void addDatasetsPanel() {
        add(new DatasetsPanel("datasets"));
    }

}
