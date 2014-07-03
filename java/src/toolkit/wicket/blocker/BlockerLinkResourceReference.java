package toolkit.wicket.blocker;

import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

import java.util.Arrays;

public class BlockerLinkResourceReference extends JavaScriptResourceReference {

    private static final BlockerLinkResourceReference instance = new BlockerLinkResourceReference();

    private BlockerLinkResourceReference() {
        super(BlockerLinkResourceReference.class, "resources/jquery.blockUI.js");
    }

    public static final BlockerLinkResourceReference get() {
        return instance;
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        JavaScriptReferenceHeaderItem jquery = JavaScriptHeaderItem.forReference(JQueryResourceReference.get());
        CssReferenceHeaderItem style = CssHeaderItem.forReference(new CssResourceReference(BlockerLinkResourceReference.class, "resources/blocker.css"));
        return Arrays.asList(jquery, style);
    }
}
