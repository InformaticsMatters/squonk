package toolkit.wicket.style.simple;

import org.apache.wicket.request.resource.CssResourceReference;

/**
 * @author simetrias
 */
public class SimpleOverridesResourceReferences {

    private static final SimpleOverridesResourceReferences instance = new SimpleOverridesResourceReferences();
    private static final CssResourceReference select2CssOverrides = new CssResourceReference(SimpleOverridesResourceReferences.class, "resources/select2-overrides.css");
    private static final CssResourceReference inmethodCssOverrides = new CssResourceReference(SimpleOverridesResourceReferences.class, "resources/inmethod-overrides.css");

    private SimpleOverridesResourceReferences() {
    }

    public static CssResourceReference getSelect2CssOverrides() {
        return select2CssOverrides;
    }

    public static CssResourceReference getInmethodCssOverrides() {
        return inmethodCssOverrides;
    }
}
