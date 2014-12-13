package com.im.lac.wicket.semantic;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

public class NotifierBehavior extends AbstractAjaxBehavior {

    private static final String DISPLAY_NAME_PLACEHOLDER = ":fieldDisplayName";
    private static final String NOTIFIER_JS = "" +
            "var markup = $('#" + DISPLAY_NAME_PLACEHOLDER + "');\n" +
            "markup.find('.notifier').popup();\n" +
            "markup.on('click', function(e) {\n" +
            "   markup.find('.notifier').hide();" +
            "})\n";
    private String markupId;

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        response.render(JavaScriptReferenceHeaderItem.forReference(SemanticResourceReference.get()));

        markupId = getComponent().getMarkupId();

        String js = NOTIFIER_JS;
        js = js.replace(DISPLAY_NAME_PLACEHOLDER, markupId);
        response.render(OnDomReadyHeaderItem.forScript(js));
    }

    protected String getMarkupId() {
        return markupId;
    }

    @Override
    public void onRequest() {
    }
}
