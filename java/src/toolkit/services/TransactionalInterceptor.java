package toolkit.services;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;

/**
 * @author simetrias
 */
public abstract class TransactionalInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        EntityManager em = getEntityManager();

        boolean act = !em.getTransaction().isActive();
        if (act) {
            em.getTransaction().begin();
        }
        try {
            Object result = ic.proceed();
            if (act) {
                em.getTransaction().commit();
            }
            return result;
        } catch (Exception e) {
            if (act) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            }
            throw e;
        }
    }

    protected abstract EntityManager getEntityManager();
}
