package toolkit.wicket.layout;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * @author simetrias
 */
public class LayoutOverridesResourceReference {

    private static final LayoutOverridesResourceReference instance = new LayoutOverridesResourceReference();
    private static final JavaScriptResourceReference resizeAccordionsOverrides = new JavaScriptResourceReference(LayoutOverridesResourceReference.class, "resources/jquery.layout.resizePaneAccordions-1.2.min.js");

    private LayoutOverridesResourceReference() {
    }

    public static JavaScriptResourceReference getResizeAccordionsOverrides() {
        return resizeAccordionsOverrides;
    }
}
