package toolkit.jersey;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author simetrias
 */
public class DateQueryParamInjectable extends AbstractHttpContextInjectable<Date> {

    private static final ThreadLocal<DateFormat> DATEFORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>();
    private final String name;

    public DateQueryParamInjectable(String name) {
        this.name = name;
    }

    @Override
    public Date getValue(HttpContext hc) {
        String string = hc.getRequest().getQueryParameters().getFirst(name);
        if (string == null || string.trim().length() == 0) {
            return null;
        }
        DateFormat dateFormat = createDateFormat();
        try {
            return dateFormat.parse(string);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private DateFormat createDateFormat() {
        DateFormat dateFormat = DATEFORMAT_THREAD_LOCAL.get();
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            DATEFORMAT_THREAD_LOCAL.set(dateFormat);
        }
        return dateFormat;
    }
}
