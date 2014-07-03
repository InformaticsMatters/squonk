package toolkit.wicket.modal;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

/**
 * @author simetrias
 */
public class ModalBehavior extends Behavior {

    private static final int DEFAULT_Z_INDEX = 1002;
    private static final String STYLE_ATTRIBUTE_DECORATION = "position: fixed; z-index: :zIndex; ";
    private static final String BLOCKER_MARKUP = "<div class='modal-blocker' style='position: fixed; z-index: :zIndex;'></div>";
    private static final String MODAL_POSITION_CALL = "modalPosition(':markupId')";
    private int zIndex;

    public ModalBehavior() {
        this(DEFAULT_Z_INDEX);
    }

    public ModalBehavior(int zIndex) {
        this.zIndex = zIndex;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        response.render(JavaScriptHeaderItem.forReference(ModalResourceReference.get()));
        response.render(OnDomReadyHeaderItem.forScript(MODAL_POSITION_CALL.replace(":markupId", component.getMarkupId())));
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        String currentStyle = tag.getAttribute("style");
        if (currentStyle == null) {
            currentStyle = "";
        }
        String newStyle = STYLE_ATTRIBUTE_DECORATION.replace(":zIndex", Integer.toString(zIndex));
        tag.put("style", newStyle + currentStyle);
    }

    @Override
    public void afterRender(Component component) {
        super.afterRender(component);
        component.getResponse().write(BLOCKER_MARKUP.replace(":zIndex", Integer.toString(zIndex - 1)));
    }

}
