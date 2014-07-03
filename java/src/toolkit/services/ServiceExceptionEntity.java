package toolkit.services;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ServiceExceptionEntity {

    private Class exceptionClass;
    private String causeMessage;
    private String causeString;
    private List<ServiceStackTraceItem> stackTraceItemList = new ArrayList<ServiceStackTraceItem>();

    public Class getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(Class exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getCauseMessage() {
        return causeMessage;
    }

    public void setCauseMessage(String causeMessage) {
        this.causeMessage = causeMessage;
    }

    public String getCauseString() {
        return causeString;
    }

    public void setCauseString(String causeString) {
        this.causeString = causeString;
    }

    public List<ServiceStackTraceItem> getStackTraceItemList() {
        return stackTraceItemList;
    }

    public void setStackTraceItemList(List<ServiceStackTraceItem> stackTraceItemList) {
        this.stackTraceItemList = stackTraceItemList;
    }

}

