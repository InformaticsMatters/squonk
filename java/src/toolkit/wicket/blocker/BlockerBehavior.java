package toolkit.wicket.blocker;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;

public class BlockerBehavior extends AbstractAjaxBehavior {

    private static final String BLOCKER_MESSAGE_PLACEHOLDER = ":blockerMessage";
    private static final String ON_BEFORE_SEND_WITH_MESSAGE = "$.blockUI({ message: '<div class=\"blocker-popup\"><div class=\"blocker\"></div><div class=\"msg\">" + BLOCKER_MESSAGE_PLACEHOLDER + "</div></div>' });";
    private static final String ON_BEFORE_SEND_WITHOUT_MESSAGE = "$.blockUI({ message: '<div class=\"blocker-popup\"><div class=\"blocker\"/></div>' });";

    public static AjaxCallListener createBlockerListener(String message) {
        AjaxCallListener ajaxCallListener = new AjaxCallListener();
        ajaxCallListener.onBeforeSend(ON_BEFORE_SEND_WITH_MESSAGE.replace(BLOCKER_MESSAGE_PLACEHOLDER, message));
        ajaxCallListener.onComplete("$.unblockUI();");
        return ajaxCallListener;
    }

    public static AjaxCallListener createBlockerListener() {
        AjaxCallListener ajaxCallListener = new AjaxCallListener();
        ajaxCallListener.onBeforeSend(ON_BEFORE_SEND_WITHOUT_MESSAGE);
        ajaxCallListener.onComplete("$.unblockUI();");
        return ajaxCallListener;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        response.render(JavaScriptReferenceHeaderItem.forReference(BlockerLinkResourceReference.get()));
    }

    @Override
    public void onRequest() {
    }

}
