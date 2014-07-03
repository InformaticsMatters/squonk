package toolkit.wicket.modal;

import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

import java.util.Arrays;

/**
 * @author simetrias
 */
public class ModalResourceReference extends JavaScriptResourceReference {

    private static final ModalResourceReference instance = new ModalResourceReference();

    private ModalResourceReference() {
        super(ModalResourceReference.class, "resources/modal.js");
    }

    public static final ModalResourceReference get() {
        return instance;
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        JavaScriptReferenceHeaderItem jquery = JavaScriptHeaderItem.forReference(JQueryResourceReference.get());
        CssReferenceHeaderItem style = CssHeaderItem.forReference(new CssResourceReference(ModalResourceReference.class, "resources/modal.css"));
        return Arrays.asList(jquery, style);
    }

}
