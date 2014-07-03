package toolkit.wicket.paginator;

import org.apache.wicket.request.resource.CssResourceReference;

/**
 * @author simetrias
 */
public class PaginatorResourceReference extends CssResourceReference {

    private static final PaginatorResourceReference instance = new PaginatorResourceReference();

    private PaginatorResourceReference() {
        super(PaginatorResourceReference.class, "resources/paginator.css");
    }

    public static final PaginatorResourceReference get() {
        return instance;
    }
}
