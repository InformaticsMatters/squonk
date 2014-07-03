package toolkit.wicket.ckeditor;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author simetrias
 */
public class CKEditable extends Panel {

    private static final String COPYTO_LISTENER = "$('#:destination').val($('#:editableMarkupId').html());";
    private static final String SET_CONTENT = "$('#:editableMarkupId').html(':content');";
    private final MarkupContainer popupContainer;
    private String content;
    private Label editable;
    private CKEditorPopup ckEditorPopup;
    private String initializationContentJs;

    public CKEditable(String id, MarkupContainer popupContainer, String content) {
        super(id);
        this.content = content;
        this.popupContainer = popupContainer;
        addEditableSupport();
    }

    public static String convertForJavaScript(String input) {
        String value = input;
        value = value.replace("\r\n", "\n");
        value = value.replace("\r", "\n");
        value = value.replace("\\", "\\\\");
        value = value.replace("\n", "\\n");
        value = value.replace("\"", "\\\"");
        value = value.replace("'", "\\'");
        return value;
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        if (initializationContentJs != null) {
            container.getHeaderResponse().render(new OnDomReadyHeaderItem(initializationContentJs));
        }
    }

    public void addEditableSupport() {
        editable = new Label("editable", content);
        editable.setEscapeModelStrings(false);
        editable.setOutputMarkupId(true);
        add(editable);

        ckEditorPopup = new CKEditorPopup("popup", popupContainer, editable.getMarkupId());
        ckEditorPopup.setCallbackHandler(new CKEditorPopup.CallbackHandler() {

            @Override
            public void onSave(AjaxRequestTarget ajaxRequestTarget) {
                ckEditorPopup.hideModal(ajaxRequestTarget);
                ajaxRequestTarget.add(popupContainer);
            }

            @Override
            public void onCancel(AjaxRequestTarget ajaxRequestTarget) {
                ckEditorPopup.hideModal(ajaxRequestTarget);
                ajaxRequestTarget.add(popupContainer);
            }
        });

        AjaxLink ajaxLink = new AjaxLink("edit") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                ckEditorPopup.showModal(ajaxRequestTarget);
                ajaxRequestTarget.add(popupContainer);
            }
        };
        add(ajaxLink);
    }

    public AjaxCallListener createCopyToListener(String destination) {
        String js = COPYTO_LISTENER.replace(":editableMarkupId", editable.getMarkupId()).replace(":destination", destination);
        AjaxCallListener ajaxCallListener = new AjaxCallListener();
        ajaxCallListener.onBefore(js);
        return ajaxCallListener;
    }

    public void setContent(String content) {
        this.content = content;
        initializationContentJs = SET_CONTENT.replace(":editableMarkupId", editable.getMarkupId()).replace(":content", convertForJavaScript(content));
    }

}

