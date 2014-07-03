package toolkit.services;

import java.util.List;

public class RemoteServiceException extends RuntimeException {

    private final String string;

    private RemoteServiceException(Throwable cause) {
        throw new RuntimeException("Unsupported");
    }

    private RemoteServiceException(String message) {
        throw new RuntimeException("Unsupported");
    }

    private RemoteServiceException(String message, Throwable throwable) {
        throw new RuntimeException("Unsupported");
    }

    public RemoteServiceException(ServiceExceptionEntity serviceExceptionEntity) {
        super(serviceExceptionEntity.getCauseMessage());
        string = serviceExceptionEntity.getCauseString();
        List<ServiceStackTraceItem> serviceStackTraceItems = serviceExceptionEntity.getStackTraceItemList();
        StackTraceElement[] stackTraceElements = new StackTraceElement[serviceStackTraceItems.size()];
        for (int i = 0; i < serviceStackTraceItems.size(); i++) {
            ServiceStackTraceItem serviceStackTraceItem = serviceStackTraceItems.get(i);
            StackTraceElement stackTraceElement = new StackTraceElement(serviceStackTraceItem.getDeclaringClass(), serviceStackTraceItem.getMethodName(), serviceStackTraceItem.getFileName(), serviceStackTraceItem.getLineNumber());
            stackTraceElements[i] = stackTraceElement;
        }
        setStackTrace(stackTraceElements);
    }

    @Override
    public String toString() {
        return string;
    }

}
