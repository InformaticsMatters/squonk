package files.api.webapp;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Bytes;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author simetrias
 */
public class FileUploadPanel extends Panel {

    private static final Logger logger = Logger.getLogger(FileUploadPanel.class.getName());
    private CallbackHandler callbackHandler;
    private FileUploadForm form;

    public FileUploadPanel(String id, boolean uploadImmediately) {
        super(id);
        form = new FileUploadForm("uploadForm", uploadImmediately);
        add(form);
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public interface CallbackHandler extends Serializable {

        void onUpload(String clientFileName, InputStream inputStream, AjaxRequestTarget target) throws IOException;

    }

    private class FileUploadForm extends Form<Void> {

        private boolean uploadImmediately;
        private FileUploadField fileUploadField;

        public FileUploadForm(String name, boolean uploadImmediately) {
            super(name);
            this.uploadImmediately = uploadImmediately;
            setMultiPart(true);
            fileUploadField = new FileUploadField("fileInput", new FileUploadFieldModel());
            add(fileUploadField);
            setMaxSize(Bytes.megabytes(10));
            if (uploadImmediately) {
                addUploadImmediatelyBehavior();
            }
            addAjaxSubmit();
        }

        private void addUploadImmediatelyBehavior() {
            fileUploadField.add(new AjaxFormSubmitBehavior("onchange") {
                @Override
                protected void onSubmit(AjaxRequestTarget target) {
                    upload(target);
                }
            });
        }

        private void addAjaxSubmit() {
            AjaxSubmitLink submitLink = new AjaxSubmitLink("submit", form) {

                @Override
                public boolean isVisible() {
                    return !uploadImmediately;
                }

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    upload(target);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    for (FeedbackMessage fm : form.getFeedbackMessages().messages(IFeedbackMessageFilter.ALL)) {
                        logger.severe(String.valueOf(fm.getMessage()));
                    }
                }
            };
            add(submitLink);
        }

        private void upload(AjaxRequestTarget target) {
            final List<FileUpload> uploads = fileUploadField.getFileUploads();
            if (uploads != null && callbackHandler != null) {
                processUploads(target, uploads);
            }
        }

        private void processUploads(AjaxRequestTarget target, List<FileUpload> uploads) {
            for (FileUpload upload : uploads) {
                try {
                    callbackHandler.onUpload(upload.getClientFileName(), upload.getInputStream(), target);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private class FileUploadFieldModel implements IModel<List<FileUpload>> {

        private List<FileUpload> objectModel = new ArrayList<FileUpload>();

        @Override
        public List<FileUpload> getObject() {
            return objectModel;
        }

        @Override
        public void setObject(List<FileUpload> fileUploads) {
        }

        @Override
        public void detach() {
        }
    }
}
