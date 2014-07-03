package toolkit.wicket.jqueryui;

import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

import java.util.Arrays;

/**
 * @author simetrias
 */
public class JQueryUIResourceReference extends JavaScriptResourceReference {

    private static final JQueryUIResourceReference instance = new JQueryUIResourceReference();

    private JQueryUIResourceReference() {
        super(JQueryUIResourceReference.class, "resources/jquery-ui-1.10.1.custom.min.js");
    }

    public static final JQueryUIResourceReference get() {
        return instance;
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        JavaScriptReferenceHeaderItem jquery = JavaScriptHeaderItem.forReference(JQueryResourceReference.get());
        CssReferenceHeaderItem style = CssHeaderItem.forReference(new CssResourceReference(JQueryUIResourceReference.class, "resources/css/smoothness/jquery-ui-1.10.1.custom.min.css"));
        return Arrays.asList(jquery, style);
    }

}
