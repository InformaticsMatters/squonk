package webapp;

import localservice.DevConfig;
import localservice.DevLocalService;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.PackageResourceReference;
import toolkit.wicket.style.simple.SimpleStyleResourceReference;

import javax.inject.Inject;

/**
 * @author simetrias
 */
public class DevHomePage extends WebPage {

    @Inject
    private DevConfig config;
    @Inject
    private DevLocalService localService;

    private WebMarkupContainer plumbContainer;
    private WebMarkupContainer source;
    private WebMarkupContainer target;

    public DevHomePage() {
        addPlumbContainer();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(SimpleStyleResourceReference.get()));
        response.render(CssHeaderItem.forReference(new PackageResourceReference(DevHomePage.class, "resources/flow.css")));
        response.render(JavaScriptHeaderItem.forReference(new PackageResourceReference(DevHomePage.class, "resources/js/dom.jsPlumb-1.6.2.js")));
        response.render(OnDomReadyHeaderItem.forScript("jsPlumb.ready(function() {" +
                "   jsPlumb.setContainer('" + plumbContainer.getMarkupId() + "');" +
                "   jsPlumb.draggable('" + source.getMarkupId() + "');" +
                "   jsPlumb.draggable('" + target.getMarkupId() + "');" +
                "   var sourceEndpointOptions = { " +
                "       isSource:true," +
                "       paintStyle : {" +
                "           fillStyle:'green'" +
                "       }," +
                "       connectorStyle : {" +
                "           strokeStyle:'green'," +
                "           lineWidth:8" +
                "       }" +
                "   };" +
                "   var sourceEndpoint = jsPlumb.addEndpoint('" + source.getMarkupId() + "', sourceEndpointOptions);" +
                "   var targetEndpointOptions = { " +
                "       isTarget:true," +
                "       paintStyle : {" +
                "           fillStyle:'green'" +
                "       }," +
                "       connectorStyle : {" +
                "           strokeStyle:'green'," +
                "           lineWidth:8" +
                "       }" +
                "   };" +
                "   var targetEndpoint = jsPlumb.addEndpoint('" + target.getMarkupId() + "', targetEndpointOptions);" +
                "});"));
    }

    private void addPlumbContainer() {
        plumbContainer = new WebMarkupContainer("plumbContainer");
        plumbContainer.setOutputMarkupId(true);
        add(plumbContainer);

        source = new WebMarkupContainer("source");
        source.setOutputMarkupId(true);
        plumbContainer.add(source);

        target = new WebMarkupContainer("target");
        target.setOutputMarkupId(true);
        plumbContainer.add(target);
    }

}
