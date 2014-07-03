package toolkit.wicket.datepicker;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import toolkit.wicket.jqueryui.JQueryUIResourceReference;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author simetrias
 */
public class DatePickerBehavior extends AbstractAjaxBehavior {

    private String markupId;
    private String dateFormat;

    public DatePickerBehavior(String dateFormat) {
        super();
        this.dateFormat = dateFormat;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        response.render(JavaScriptHeaderItem.forReference(JQueryUIResourceReference.get()));
        markupId = getComponent().getMarkupId();
        response.render(OnDomReadyHeaderItem.forScript("$('#" + markupId + "').datepicker({ dateFormat: '" + getDatePickerFormat() + "'});"));
    }

    private String getDatePickerFormat() {
        String datePickerFormat = dateFormat;
        String platformSeparator = new SimpleDateFormat("/").format(new Date());
        datePickerFormat = datePickerFormat.replaceAll("(?<!(?:').?)\\/", platformSeparator);
        datePickerFormat = datePickerFormat.replaceAll("MM", "mm");
        datePickerFormat = datePickerFormat.replaceAll("yy", "y");
        return datePickerFormat;
    }

    @Override
    public void onRequest() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
