package toolkit.wicket.datepicker;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.convert.IConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author simetrias
 */
public class DatePicker extends TextField<Date> {

    private final IConverter<Date> converter;
    private SimpleDateFormat simpleDateFormat;

    public DatePicker(String id, String dateFormat) {
        super(id);

        DatePickerBehavior datePickerBehavior = new DatePickerBehavior(dateFormat);
        add(datePickerBehavior);

        simpleDateFormat = new SimpleDateFormat(dateFormat);
        converter = new DatePickerConverter();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> IConverter<C> getConverter(Class<C> type) {
        return (IConverter<C>) converter;
    }

    class DatePickerConverter implements IConverter<Date> {

        @Override
        public Date convertToObject(String s, Locale locale) {
            try {
                return simpleDateFormat.parse(s);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String convertToString(Date date, Locale locale) {
            return simpleDateFormat.format(date);
        }
    }

}
