package toolkit.wicket.download;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author simetrias
 */
public abstract class DynamicContentAjaxDownload extends AbstractAjaxBehavior implements Serializable {

    private String contentType;
    private String fileName;
    private ContentDisposition contentDisposition;

    public DynamicContentAjaxDownload() {
        setContentDisposition(ContentDisposition.ATTACHMENT);
        setContentType("application/pdf");
        setFileName("report.pdf");
    }

    public void scheduleDownload() {
        String url = getCallbackUrl().toString();
        url = url + (url.contains("?") ? "&" : "?");
        url = url + "antiCache=" + System.currentTimeMillis();
        AjaxRequestTarget ajaxRequestTarget = RequestCycle.get().find(AjaxRequestTarget.class);
        ajaxRequestTarget.appendJavaScript("setTimeout(\"window.location.href='" + url + "'\", 100);");
    }

    @Override
    public void onRequest() {
        DynamicContentStreamWriter dynamicContentStreamWriter = new DynamicContentStreamWriter(generateReportStream(), getContentType());
        ResourceStreamRequestHandler resourceStreamRequestHandler = new ResourceStreamRequestHandler(dynamicContentStreamWriter);
        resourceStreamRequestHandler.setContentDisposition(getContentDisposition());
        resourceStreamRequestHandler.setFileName(getFileName());
        RequestCycle.get().scheduleRequestHandlerAfterCurrent(resourceStreamRequestHandler);
    }

    protected String getContentType() {
        return contentType;
    }

    protected void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected String getFileName() {
        return fileName;
    }

    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }

    protected ContentDisposition getContentDisposition() {
        return contentDisposition;
    }

    protected void setContentDisposition(ContentDisposition contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    protected abstract InputStream generateReportStream();

    class DynamicContentStreamWriter extends AbstractResourceStreamWriter {

        private final InputStream reportStream;

        public DynamicContentStreamWriter(InputStream reportStream, String contentType) {
            this.reportStream = reportStream;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            transfer(reportStream, outputStream);
        }

        @Override
        public String getContentType() {
            return DynamicContentAjaxDownload.this.getContentType();
        }

        private void transfer(InputStream inputStream, OutputStream outputStream) throws IOException {
            byte[] buffer = new byte[4096];
            int r = inputStream.read(buffer, 0, buffer.length);
            while (r > -1) {
                outputStream.write(buffer, 0, r);
                r = inputStream.read(buffer, 0, buffer.length);
            }
        }
    }

}
