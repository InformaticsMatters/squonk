package toolkit.wicket.modal;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class ModalPanel extends Panel {

    private ModalController modalController;

    public ModalPanel(String id, MarkupContainer parentMarkupContainer) {
        this(id, parentMarkupContainer, 0);
    }

    public ModalPanel(String id, MarkupContainer parentMarkupContainer, int zIndex) {
        super(id);
        setOutputMarkupId(true);

        modalController = new ModalController(parentMarkupContainer, this);

        if (zIndex == 0) {
            add(new ModalBehavior());
        } else {
            add(new ModalBehavior(zIndex));
        }
    }

    public void showModal(AjaxRequestTarget ajaxRequestTarget) {
        modalController.showModal(ajaxRequestTarget);
    }

    public void hideModal(AjaxRequestTarget ajaxRequestTarget) {
        modalController.hideModal(ajaxRequestTarget);
    }
}
