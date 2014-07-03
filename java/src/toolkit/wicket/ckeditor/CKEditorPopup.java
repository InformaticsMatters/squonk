package toolkit.wicket.ckeditor;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import toolkit.wicket.modal.ModalPanel;

import java.io.Serializable;

/**
 * @author simetrias
 */
public class CKEditorPopup extends ModalPanel {

    private final String editableMarkupId;
    private CallbackHandler callbackHandler;
    private CKEditor ckEditor;

    public CKEditorPopup(String id, MarkupContainer parentMarkupContainer, String editableMarkupId) {
        super(id, parentMarkupContainer);
        this.editableMarkupId = editableMarkupId;
        addEditor();
        addActions();
    }

    private void addActions() {
        add(new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbackHandler.onSave(ajaxRequestTarget);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.getAjaxCallListeners().add(ckEditor.createSaveListener());
            }
        });

        add(new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbackHandler.onCancel(ajaxRequestTarget);
            }
        });
    }

    private void addEditor() {
        ckEditor = new CKEditor("editor", editableMarkupId);
        add(ckEditor);
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public interface CallbackHandler extends Serializable {

        void onSave(AjaxRequestTarget ajaxRequestTarget);

        void onCancel(AjaxRequestTarget ajaxRequestTarget);

    }
}
