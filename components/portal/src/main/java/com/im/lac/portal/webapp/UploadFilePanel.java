package com.im.lac.portal.webapp;

import com.im.lac.portal.service.DatasetInputStreamFormat;
import com.im.lac.portal.service.PrototypeServiceMock;
import com.im.lac.wicket.semantic.SemanticModalPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UploadFilePanel extends SemanticModalPanel {

    @Inject
    private PrototypeServiceMock prototypeServiceMock;

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
                Map<String, Class> properties = new HashMap<String, Class>();
                properties.put("set", Integer.class);
                prototypeServiceMock.createDataset(DatasetInputStreamFormat.SDF, inputStream, properties);
            }

            @Override
            public String beforeSubmit() {
                return "document.getElementById('" + submit.getMarkupId() + "').disabled = true;";
            }

            @Override
            public String afterSubmit() {
                return "document.getElementById('" + submit.getMarkupId() + "').disabled = false;";
            }
        });
        form.add(fileUploadPanel);
    }

}
