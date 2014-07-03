package toolkit.test;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;

@RequestScoped
public class TestContext {
    private BeanManager beanManager;

    public BeanManager getBeanManager() {
        return beanManager;
    }

    public void setBeanManager(BeanManager beanManager) {
        this.beanManager = beanManager;
    }
}
