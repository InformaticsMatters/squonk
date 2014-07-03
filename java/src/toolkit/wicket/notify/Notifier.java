package toolkit.wicket.notify;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * @author simetrias
 */
public class Notifier extends WebMarkupContainer {

    private NotifierBehavior notifierBehavior;

    public Notifier(String id) {
        super(id);

        notifierBehavior = new NotifierBehavior();
        add(notifierBehavior);
    }

    public void notify(String title, String message) {
        getRequestCycle().find(AjaxRequestTarget.class).appendJavaScript("$('#" + notifierBehavior.getMarkupId() + "').notify('create', 'basic-template', {title: '" + title + "', text: '" + message + "'});");
    }
}
