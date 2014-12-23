package com.im.lac.portal.webapp;

import com.im.lac.portal.service.api.DatasetDescriptor;
import com.im.lac.portal.service.api.DatasetInputStreamFormat;
import com.im.lac.portal.service.mock.DatasetServiceMock;
import com.im.lac.wicket.semantic.SemanticModalPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UploadModalPanel extends SemanticModalPanel {

    @Inject
    private DatasetServiceMock prototypeServiceMock;
    private Callbacks callbacks;
    private DatasetDescriptor datasetDescriptor;

    public UploadModalPanel(String id, String modalElementWicketId) {
        super(id, modalElementWicketId);
        addForm();
    }

    private void addForm() {
        Form form = new Form("form");
        form.setOutputMarkupId(true);
        getModalRootComponent().add(form);
        form.setModel(new CompoundPropertyModel<UploadModalData>(new UploadModalData()));

        final AjaxSubmitLink submit = new AjaxSubmitLink("submit") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                callbacks.onSubmit();
            }
        };
        submit.setOutputMarkupId(true);
        form.add(submit);

        AjaxLink cancelAction = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                callbacks.onCancel();
            }
        };
        form.add(cancelAction);

        FileUploadPanel fileUploadPanel = new FileUploadPanel("upload", true);
        fileUploadPanel.setCallbackHandler(new FileUploadPanel.CallbackHandler() {

            @Override
            public void onUpload(String clientFileName, InputStream inputStream, AjaxRequestTarget target) throws IOException {
                Map<String, Class> properties = new HashMap<String, Class>();
                datasetDescriptor = prototypeServiceMock.createDataset(DatasetInputStreamFormat.SDF, inputStream, properties);
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

        TextField<String> descriptionField = new TextField<String>("description");
        form.add(descriptionField);
    }

    public DatasetDescriptor getDatasetDescriptor() {
        return datasetDescriptor;
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public interface Callbacks extends Serializable {

        void onSubmit();

        void onCancel();

    }

}
