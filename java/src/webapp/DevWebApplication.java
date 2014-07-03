package webapp;

import org.apache.wicket.Page;
import org.apache.wicket.cdi.CdiConfiguration;
import org.apache.wicket.protocol.http.WebApplication;
import org.jboss.weld.environment.servlet.Listener;

import javax.enterprise.inject.spi.BeanManager;

/**
 * @author simetrias
 */
public class DevWebApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return DevHomePage.class;
    }

    @Override
    protected void init() {
        super.init();
        BeanManager manager = (BeanManager) getServletContext().getAttribute(Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
        new CdiConfiguration(manager).configure(this);
    }

}
