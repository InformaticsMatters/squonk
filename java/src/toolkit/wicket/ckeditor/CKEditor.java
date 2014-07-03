package toolkit.wicket.ckeditor;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;

/**
 * @author simetrias
 */
public class CKEditor extends WebMarkupContainer {

    private static final String COPY_TO_EDITOR = "CKEDITOR.instances.:editor.setData($('#:editable').html());";
    private static final String COPY_TO_EDITABLE = "$('#:editable').html(CKEDITOR.instances.:editor.getData());";
    private static final String REPLACE_ID = "CKEDITOR.replace(':id', {height: '270px'})";
    private final String editableMarkupId;

    public CKEditor(String id, String editableMarkupId) {
        super(id);
        this.editableMarkupId = editableMarkupId;
        setOutputMarkupId(true);
    }

    @Override
    public void renderHead(HtmlHeaderContainer container) {
        super.renderHead(container);
        IHeaderResponse response = container.getHeaderResponse();
        response.render(JavaScriptReferenceHeaderItem.forReference(CKEditorResourceReference.get()));
        response.render(OnDomReadyHeaderItem.forScript(REPLACE_ID.replace(":id", getMarkupId())));
        initializeContents(response);
    }

    private void initializeContents(IHeaderResponse response) {
        response.render(OnDomReadyHeaderItem.forScript(COPY_TO_EDITOR
                .replace(":editor", getMarkupId())
                .replace(":editable", editableMarkupId)));
    }

    public AjaxCallListener createSaveListener() {
        return new AjaxCallListener() {

            @Override
            public CharSequence getBeforeHandler(Component c) {
                return COPY_TO_EDITABLE
                        .replace(":editor", getMarkupId())
                        .replace(":editable", editableMarkupId);
            }
        };
    }

    public void setData(AjaxRequestTarget ajaxRequestTarget, String data) {
        if (data == null) {
            data = "";
        }

        ajaxRequestTarget.appendJavaScript(COPY_TO_EDITOR
                .replace(":editor", getMarkupId())
                .replace(":hiddenContent", editableMarkupId)
                .replace(":data", data));
    }
}
