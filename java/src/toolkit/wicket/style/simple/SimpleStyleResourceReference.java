package toolkit.wicket.style.simple;

import org.apache.wicket.request.resource.CssResourceReference;

/**
 * @author simetrias
 */
public class SimpleStyleResourceReference extends CssResourceReference {

    private static final SimpleStyleResourceReference instance = new SimpleStyleResourceReference();

    private SimpleStyleResourceReference() {
        super(SimpleStyleResourceReference.class, "resources/simple.css");
    }

    public static SimpleStyleResourceReference get() {
        return instance;
    }

}
