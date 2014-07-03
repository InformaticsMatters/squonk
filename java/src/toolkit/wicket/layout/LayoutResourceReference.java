package toolkit.wicket.layout;

import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import toolkit.wicket.jqueryui.JQueryUIResourceReference;

import java.util.Arrays;

public class LayoutResourceReference extends JavaScriptResourceReference {

    private static final LayoutResourceReference instance = new LayoutResourceReference();

    private LayoutResourceReference() {
        super(LayoutResourceReference.class, "resources/jquery.layout-latest.min.js");
    }

    public static final LayoutResourceReference get() {
        return instance;
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        JavaScriptReferenceHeaderItem jquery = JavaScriptHeaderItem.forReference(JQueryResourceReference.get());
        JavaScriptReferenceHeaderItem ui = JavaScriptHeaderItem.forReference(JQueryUIResourceReference.get());
        CssReferenceHeaderItem style = CssHeaderItem.forReference(new CssResourceReference(LayoutResourceReference.class, "resources/layout-default-latest.css"));
        return Arrays.asList(jquery, ui, style);
    }
}