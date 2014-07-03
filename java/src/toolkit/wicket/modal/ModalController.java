package toolkit.wicket.modal;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class ModalController implements Serializable {

    private Panel modalPanel;
    private WebMarkupContainer modalPanelMock;
    private MarkupContainer markupContainer;

    public ModalController(MarkupContainer markupContainer, Panel modalPanel) {
        this.markupContainer = markupContainer;
        this.modalPanel = modalPanel;
        this.modalPanel.setOutputMarkupId(true);
        this.modalPanel.setOutputMarkupPlaceholderTag(true);
        modalPanelMock = new WebMarkupContainer(modalPanel.getId());
        modalPanelMock.setOutputMarkupPlaceholderTag(true);
        modalPanelMock.setOutputMarkupId(true);
        markupContainer.addOrReplace(modalPanelMock);
    }

    public void showModal(AjaxRequestTarget ajaxRequestTarget) {
        markupContainer.addOrReplace(modalPanel);
    }

    public void hideModal(AjaxRequestTarget ajaxRequestTarget) {
        markupContainer.addOrReplace(modalPanelMock);
    }

}

