package toolkit.wicket.ckeditor;

import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

import javax.inject.Inject;
import java.util.Arrays;

public class CKEditorResourceReference extends JavaScriptResourceReference {

    private static final CssResourceReference contentsCssResourceReference = new CssResourceReference(CKEditorResourceReference.class, "resources/contents.css");
    private static final CKEditorResourceReference instance = new CKEditorResourceReference();
    @Inject
    private CKEditorBasePathProvider ckEditorBasePathProvider;

    private CKEditorResourceReference() {
        super(CKEditorResourceReference.class, "resources/ckeditor.js");
        CdiContainer.get().getNonContextualManager().postConstruct(this);
    }

    public static final CKEditorResourceReference get() {
        return instance;
    }

    public static final CssResourceReference getContentsCssResourceReference() {
        return contentsCssResourceReference;
    }

    @Override
    public Iterable<? extends HeaderItem> getDependencies() {
        JavaScriptReferenceHeaderItem jquery = JavaScriptHeaderItem.forReference(JQueryResourceReference.get());
        JavaScriptContentHeaderItem ckeditorBasePath = JavaScriptHeaderItem.forScript(ckEditorBasePathProvider.getCKEditorBasePath(), null);
        CssReferenceHeaderItem style = CssHeaderItem.forReference(contentsCssResourceReference);
        return Arrays.asList(jquery, ckeditorBasePath, style);
    }

}
