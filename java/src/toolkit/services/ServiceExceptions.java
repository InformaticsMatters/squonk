package toolkit.services;

import com.sun.jersey.api.client.UniformInterfaceException;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
public class ServiceExceptions {

    private static Throwable findCause(Throwable parent) {
        Throwable cause = parent.getCause();
        if (cause == null) {
            return parent;
        }
        while (parent != cause) {
            parent = cause;
            cause = parent.getCause();
            if (cause == null) {
                return parent;
            }
        }
        return cause;
    }

    public WebApplicationException createServiceException(Exception exception) {
        Throwable cause = findCause(exception);

        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        ServiceExceptionEntity serviceExceptionEntity = new ServiceExceptionEntity();
        serviceExceptionEntity.setExceptionClass(exception.getClass());
        serviceExceptionEntity.setCauseString(cause.toString());

        String message = cause.getMessage();
        if (message != null) {
            serviceExceptionEntity.setCauseMessage(message);
        }

        List<ServiceStackTraceItem> elements = new ArrayList<ServiceStackTraceItem>();
        for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
            ServiceStackTraceItem item = new ServiceStackTraceItem();
            item.setDeclaringClass(stackTraceElement.getClassName());
            item.setMethodName(stackTraceElement.getMethodName());
            item.setFileName(stackTraceElement.getFileName());
            item.setLineNumber(stackTraceElement.getLineNumber());
            serviceExceptionEntity.getStackTraceItemList().add(item);
        }
        serviceExceptionEntity.getStackTraceItemList().addAll(elements);

        Response response = Response.status(status).entity(serviceExceptionEntity).type(MediaType.APPLICATION_XML).build();
        return new WebApplicationException(exception, response);
    }

    public ServiceExceptionEntity getExceptionEntity(UniformInterfaceException e) {
        return e.getResponse().getEntity(ServiceExceptionEntity.class);
    }

    public String toHtml(ServiceExceptionEntity serviceExceptionEntity) {
        String html = "<html><ol>";
        html = html + "<li>toString message: " + serviceExceptionEntity.getCauseString() + "</li>";
        String message = serviceExceptionEntity.getCauseMessage();
        if (message != null) {
            html = html + "<li>Message: " + message + "</li>";
        }
        for (ServiceStackTraceItem item : serviceExceptionEntity.getStackTraceItemList()) {
            html = html + "<li>" + item.getMethodName() + " - " + item.getDeclaringClass() + " [" + item.getLineNumber() + "]</li>";
        }
        html = html + "</ol></html>";

        return html;
    }

}
