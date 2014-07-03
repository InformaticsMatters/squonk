package toolkit.wicket.notify;

import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;
import toolkit.wicket.jqueryui.JQueryUIResourceReference;

import java.util.Arrays;

public class NotifierResourceReference extends JavaScriptResourceReference {

    private static final NotifierResourceReference instance = new NotifierResourceReference();

    private NotifierResourceReference() {
        super(NotifierResourceReference.class, "resources/jquery.notify.min.js");
    }

    public static final NotifierResourceReference get() {
        return instance;
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        JavaScriptReferenceHeaderItem jquery = JavaScriptHeaderItem.forReference(JQueryResourceReference.get());
        JavaScriptReferenceHeaderItem ui = JavaScriptHeaderItem.forReference(JQueryUIResourceReference.get());
        CssReferenceHeaderItem style = CssHeaderItem.forReference(new CssResourceReference(NotifierResourceReference.class, "resources/ui.notify.css"));
        return Arrays.asList(jquery, ui, style);
    }

}
