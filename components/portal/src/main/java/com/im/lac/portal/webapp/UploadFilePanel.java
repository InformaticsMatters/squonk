package com.im.lac.portal.webapp;

import com.im.lac.files.api.webapp.FileUploadPanel;
import com.im.lac.files.api.webservice.Files;
import com.im.lac.wicket.semantic.SemanticModalPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author simetrias
 */
public class UploadFilePanel extends SemanticModalPanel {

    //@Inject
    private Files files;
    private String tempFolderName;

    public UploadFilePanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {
        Form form = new Form("form");
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);

        final AjaxSubmitLink submit = new AjaxSubmitLink("save") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
            }
        };
        submit.setOutputMarkupId(true);
        form.add(submit);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                hideModal();
            }
        };
        form.add(cancelAction);

        FileUploadPanel fileUploadPanel = new FileUploadPanel("upload", true);
        fileUploadPanel.setCallbackHandler(new FileUploadPanel.CallbackHandler() {

            @Override
            public void onUpload(String clientFileName, InputStream inputStream, AjaxRequestTarget target) throws IOException {
                files.createTempFile(tempFolderName, clientFileName, inputStream);
            }

            @Override
            public String beforeSubmit() {
                String js = "document.getElementById('" + submit.getMarkupId() + "').disabled = true;";
                return js;
            }

            @Override
            public String afterSubmit() {
                String js = "document.getElementById('" + submit.getMarkupId() + "').disabled = false;";
                return js;
            }
        });
        form.add(fileUploadPanel);
    }

}
